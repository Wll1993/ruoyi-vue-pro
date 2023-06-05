package com.cw.module.trade.service.account;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cw.module.trade.enums.ErrorCodeConstants.ACCOUNT_NOT_EXISTS;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.NewOrderRespType;
import com.binance.client.model.enums.OrderSide;
import com.binance.client.model.enums.OrderType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.enums.TimeInForce;
import com.cw.module.trade.controller.admin.account.vo.AccountCreateReqVO;
import com.cw.module.trade.controller.admin.account.vo.AccountExportReqVO;
import com.cw.module.trade.controller.admin.account.vo.AccountPageReqVO;
import com.cw.module.trade.controller.admin.account.vo.AccountUpdateReqVO;
import com.cw.module.trade.controller.admin.syncrecord.vo.SyncRecordCreateReqVO;
import com.cw.module.trade.convert.account.AccountConvert;
import com.cw.module.trade.dal.dataobject.account.AccountDO;
import com.cw.module.trade.dal.mysql.account.AccountMapper;
import com.cw.module.trade.handler.WebSocketHandlerFactory;
import com.cw.module.trade.service.syncrecord.SyncRecordService;
import com.tb.utils.NumberUtils;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.log.Log;

/**
 * 交易账号 Service 实现类
 *
 * @author chengjiale
 */
@Slf4j
@Service
@Validated
public class AccountServiceImpl implements AccountService {

    @Resource
    private AccountMapper accountMapper;
    
    @Autowired
    private SyncRecordService syncRecordServiceImpl;

    @Override
    public Long createAccount(AccountCreateReqVO createReqVO) {
        // 插入
        AccountDO account = AccountConvert.INSTANCE.convert(createReqVO);
        accountMapper.insert(account);
        
        // 将新增账号添加到监听队列中去,并且同步主账户的交易对的杠杆倍率
        WebSocketHandlerFactory.get().addHandler(account);
        if(NumberUtils.gtz(account.getFollowAccount())) {
            WebSocketHandlerFactory.get().syncAToBAccount(
                    accountMapper.selectById(account.getFollowAccount()), account);
        }
        // 返回
        return account.getId();
    }

    @Override
    public void updateAccount(AccountUpdateReqVO updateReqVO) {
        // 校验存在
        validateAccountExists(updateReqVO.getId());
        // 更新
        AccountDO originalAccount = accountMapper.selectById(updateReqVO.getId());
        AccountDO updateObj = AccountConvert.INSTANCE.convert(updateReqVO);
        accountMapper.updateById(updateObj);
        
        // 跟随账户变更后需要重新设置交易对的杠杆倍率
        if(NumberUtils.gtz(updateReqVO.getFollowAccount()) &&
                NumberUtils.equals(updateReqVO.getFollowAccount(), originalAccount.getFollowAccount())) {
            WebSocketHandlerFactory.get().syncAToBAccount(
                    accountMapper.selectById(updateReqVO.getFollowAccount()), originalAccount);
        }
    }

    @Override
    public void deleteAccount(Long id) {
        // 校验存在
        validateAccountExists(id);
        // 删除
        accountMapper.deleteById(id);
    }

    private void validateAccountExists(Long id) {
        if (accountMapper.selectById(id) == null) {
            throw exception(ACCOUNT_NOT_EXISTS);
        }
    }

    @Override
    public AccountDO getAccount(Long id) {
        return accountMapper.selectById(id);
    }

    @Override
    public List<AccountDO> getAccountList(Collection<Long> ids) {
        return accountMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<AccountDO> getAccountPage(AccountPageReqVO pageReqVO) {
        return accountMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AccountDO> getAccountList(AccountExportReqVO exportReqVO) {
        return accountMapper.selectList(exportReqVO);
    }
    
    @Override
    public String syncBalance(Long accountId) {
        if(NumberUtils.gtz(accountId)) {
            AccountDO account = accountMapper.selectById(accountId);
            return syncThirdBalance(account);
        }
        List<AccountDO> accounts = accountMapper.selectList();
        for(AccountDO account : accounts) {
            syncThirdBalance(account);
        }
        return "";
    }

    private String syncThirdBalance(AccountDO account) {
        try {
            RequestOptions options = new RequestOptions();
            SyncRequestClient syncRequestClient = SyncRequestClient.create(account.getAppKey(), 
                    account.getAppSecret(), options);
            String balance = JSONUtil.toJsonStr(syncRequestClient.getBalance());
            account.setBalance(balance);
            account.setLastBalanceQueryTime(System.currentTimeMillis());
            accountMapper.updateById(account);
            
            // 保存同步记录
            SyncRecordCreateReqVO syncRecord = new SyncRecordCreateReqVO();
            syncRecord.setAccountId(account.getId());
            syncRecord.setType("balance");
            syncRecord.setThirdData(balance);
            syncRecordServiceImpl.createSyncRecord(syncRecord);
            
            return balance;
        } catch (Exception e) {
            log.error("同步账号余额发生错误,账号：{}, 异常:{}", account, e);
        }
        return "";
    }

    
    
    public Boolean buy(Long accountId) {
        RequestOptions options = new RequestOptions();
        AccountDO account = getAccount(accountId);
        SyncRequestClient syncRequestClient = SyncRequestClient.create(account.getAppKey(), account.getAppSecret(),
                options);
        System.out.println(syncRequestClient.postOrder(
                "BTCUSDT",  //symbol    交易对
                OrderSide.SELL, //side  买卖方向
                PositionSide.SHORT, //positionSide  持仓方向，单向持仓模式下非必填，默认且仅可填BOTH;在双向持仓模式下必填,且仅可选择 LONG 或 SHORT
                OrderType.LIMIT,    // orderType 订单类型 LIMIT, MARKET, STOP, TAKE_PROFIT, STOP_MARKET, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET
                TimeInForce.GTC,    // timeInForce  有效方法
                "1",    // quantity     下单数量,使用closePosition不支持此参数。
                "9000", // price    委托价格
                null,   // reduceOnly true, false; 非双开模式下默认false；双开模式下不接受此参数； 使用closePosition不支持此参数。
                null,   // newClientOrderId 用户自定义的订单号，不可以重复出现在挂单中。如空缺系统会自动赋值。必须满足正则规则 ^[\.A-Z\:/a-z0-9_-]{1,36}$
                null,   // stopPrice 触发价, 仅 STOP, STOP_MARKET, TAKE_PROFIT, TAKE_PROFIT_MARKET 需要此参数
                null,   // workingType stopPrice 触发类型: MARK_PRICE(标记价格), CONTRACT_PRICE(合约最新价). 默认 CONTRACT_PRICE
                NewOrderRespType.RESULT)); //newOrderRespType "ACK", "RESULT", 默认 "ACK"
        
        // closePosition   true, false；触发后全部平仓，仅支持STOP_MARKET和TAKE_PROFIT_MARKET；不与quantity合用；自带只平仓效果，不与reduceOnly 合用
        // activationPrice  追踪止损激活价格，仅TRAILING_STOP_MARKET 需要此参数, 默认为下单当前市场价格(支持不同workingType)
        // callbackRate   追踪止损回调比例，可取值范围[0.1, 5],其中 1代表1% ,仅TRAILING_STOP_MARKET 需要此参数
        // priceProtect     条件单触发保护："TRUE","FALSE", 默认"FALSE". 仅 STOP, STOP_MARKET, TAKE_PROFIT, TAKE_PROFIT_MARKET 需要此参数
        // recvWindow 延迟时间
        // timestamp 当前时间戳
        return false;
    }

    @Override
    public List<AccountDO> listMonitorAccount() {
        return accountMapper.listMonitorAccount();
    }
    
    @Override
    public List<AccountDO> listFollowAccount(Long accountId) {
        return accountMapper.selectList(Wrappers.lambdaQuery(new AccountDO().setFollowAccount(accountId)));
    }
    
    
}
