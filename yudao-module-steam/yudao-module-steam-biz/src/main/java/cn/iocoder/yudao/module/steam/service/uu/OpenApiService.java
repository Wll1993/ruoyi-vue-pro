package cn.iocoder.yudao.module.steam.service.uu;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.infra.dal.dataobject.config.ConfigDO;
import cn.iocoder.yudao.module.infra.service.config.ConfigService;
import cn.iocoder.yudao.module.steam.controller.app.vo.ApiResult;
import cn.iocoder.yudao.module.steam.controller.app.vo.OpenApiReqVo;
import cn.iocoder.yudao.module.steam.dal.dataobject.devaccount.DevAccountDO;
import cn.iocoder.yudao.module.steam.enums.OpenApiCode;
import cn.iocoder.yudao.module.steam.service.devaccount.DevAccountService;
import cn.iocoder.yudao.module.steam.utils.HttpUtil;
import cn.iocoder.yudao.module.steam.utils.JacksonUtils;
import cn.iocoder.yudao.module.steam.utils.RSAUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 开放平台接口
 * @author glzaboy
 */
@Service
@Slf4j
public class OpenApiService {
    @Resource
    private DevAccountService accountService;
    private Validator validator;

    @Resource
    private ConfigService configService;

    @Resource
    private ObjectMapper objectMapper;

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }
    public <T extends Serializable> DevAccountDO apiCheck(OpenApiReqVo<T> openApiReqVo) {
        log.info("收到开发平台接口调用{}", JsonUtils.toJsonString(openApiReqVo));
        Set<ConstraintViolation<OpenApiReqVo<T>>> validate = validator.validate(openApiReqVo);
        if(!validate.isEmpty()){
            throw new ServiceException(OpenApiCode.JACKSON_EXCEPTION);
        }
        DevAccountDO devAccountDO = accountService.selectByUserName(openApiReqVo.getAppKey(), UserTypeEnum.MEMBER);
        if(Objects.isNull(devAccountDO)){
            throw new ServiceException(OpenApiCode.ID_ERROR);
        }
        if(CommonStatusEnum.isDisable(devAccountDO.getStatus())){
            throw new ServiceException(OpenApiCode.DISABLED);
        }
        checkSign(openApiReqVo,devAccountDO.getApiPublicKey());
        return devAccountDO;
    }
    /**
     * 有品请求公共实现类
     * @param openApiReqVo 有品传入参数，只需要管data
     * @param <T> 入参类型
     * @return 签名后的json对象
     */
    public <T extends Serializable> OpenApiReqVo<T> requestUUSign(OpenApiReqVo<T> openApiReqVo){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ConfigDO configApiKey = configService.getConfigByKey("uu.appKey");
        ConfigDO configByKey = configService.getConfigByKey("uu.prikey");
        String key=configByKey.getValue();
        openApiReqVo.setTimestamp(simpleDateFormat.format(new Date()));
        openApiReqVo.setAppKey(configApiKey.getValue());
        sign(openApiReqVo,key);
        return openApiReqVo;
    }
    /**
     * 有品请求公共实现类
     * @param url 接口地址
     * @param openApiReqVo 有品传入参数，只需要管data
     * @param classic 返回数据格式
     * @param <T> 入参类型
     * @param <E> 出参类型
     * @return UU数据结果
     */
    public <T extends Serializable,E extends Serializable> ApiResult<E> requestUU(String url, OpenApiReqVo<T> openApiReqVo, Class<E> classic){

        OpenApiReqVo<T> tOpenApiReqVo = requestUUSign(openApiReqVo);
        HttpUtil.HttpRequest.HttpRequestBuilder builder = HttpUtil.HttpRequest.builder();
        builder.url(url);
        builder.method(HttpUtil.Method.JSON);
        builder.postObject(tOpenApiReqVo);
        HttpUtil.HttpResponse sent = HttpUtil.sent(builder.build());
        ApiResult json = sent.json(ApiResult.class);
        Object data = json.getData();
        try {
            ApiResult<E> apiResult=new ApiResult<>();
            E e1 = objectMapper.readValue(objectMapper.writeValueAsString(data), classic);
            apiResult.setData(e1);
            apiResult.setMsg(json.getMsg());
            apiResult.setTimestamp(json.getTimestamp());
            apiResult.setCode(json.getCode());
            return apiResult;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServiceException(-1,"格式转换出错");
        }
    }
    /**
     * 参数签名兼容有品
     * 出现错误时会抛出异常
     * @param openApiReqVo 入参
     * @param pubKey 公钥
     * @param <T> 入参类型
     */
    public <T extends Serializable> void checkSign(OpenApiReqVo<T> openApiReqVo, String pubKey) {
        try{
            //时间检测
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parse = simpleDateFormat.parse(openApiReqVo.getTimestamp());
            long l = (System.currentTimeMillis() - parse.getTime()) / 1000;
            if(l>600){
                throw new ServiceException(OpenApiCode.CHECK_SIGN_ERROR);
            }
            Map<String, Object> params = new HashMap<>();
            params.put("timestamp",openApiReqVo.getTimestamp());
            params.put("appKey",openApiReqVo.getAppKey());
            T data = openApiReqVo.getData();
            if(Objects.nonNull(data)){
                Field[] declaredFields = data.getClass().getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    String name = declaredField.getName();
                    declaredField.setAccessible(true);
                    params.put(name,declaredField.get(data));
                }
            }

            // 第一步：检查参数是否已经排序
            String[] keys = params.keySet().toArray(new String[0]);
            Arrays.sort(keys);
            // 第二步：把所有参数名和参数值串在一起
            StringBuilder stringBuilder = new StringBuilder();
            for (String key : keys) {
                Object value = params.get(key);
                if (Objects.nonNull(value)) {
                    stringBuilder.append(key).append(JacksonUtils.writeValueAsString(value));
                }
            }
            //采用私钥签名
            boolean b = RSAUtils.verifyByPublicKey(stringBuilder.toString().getBytes(), pubKey, openApiReqVo.getSign());
            log.info("签名比较结果{}",b);
            if(!b){
                throw new ServiceException(OpenApiCode.CHECK_SIGN_ERROR);
            }
        }catch (InvalidKeySpecException | IllegalAccessException | InvalidKeyException | SignatureException | NoSuchAlgorithmException | ParseException e) {
            e.printStackTrace();
            log.error("解密出错原因{}",e.getMessage());
            throw new ServiceException(OpenApiCode.CHECK_SIGN_ERROR);
        }
    }
    /**
     * 参数签名兼容有品
     * @param openApiReqVo 入参
     * @param priKey 私钥
     * @param <T> 入参类型
     */
    private  <T extends Serializable> void sign(OpenApiReqVo<T> openApiReqVo, String priKey) {
        try{
            Map<String, Object> params = new HashMap<>();
            params.put("timestamp",openApiReqVo.getTimestamp());
            params.put("appKey",openApiReqVo.getAppKey());
            T data = openApiReqVo.getData();
            if(Objects.nonNull(data)){
                Field[] declaredFields = data.getClass().getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    String name = declaredField.getName();
                    declaredField.setAccessible(true);
                    params.put(name,declaredField.get(data));
                }
            }

            // 第一步：检查参数是否已经排序
            String[] keys = params.keySet().toArray(new String[0]);
            Arrays.sort(keys);
            // 第二步：把所有参数名和参数值串在一起
            StringBuilder stringBuilder = new StringBuilder();
            for (String key : keys) {
                Object value = params.get(key);
                if (Objects.nonNull(value)) {
                    stringBuilder.append(key).append(JacksonUtils.writeValueAsString(value));
                }
            }
            try {
                String sign = RSAUtils.signByPrivateKey(stringBuilder.toString().getBytes(), priKey);
                openApiReqVo.setSign(sign);
                log.info("签名sign:{}", JacksonUtils.writeValueAsString(openApiReqVo));
            } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | InvalidKeySpecException e) {
                e.printStackTrace();
                throw new ServiceException(OpenApiCode.SIGN_ERROR);
            }
        }catch (IllegalAccessException e){
            log.error("检查签名出错类不可访问{}",e.getMessage());
            throw new ServiceException(OpenApiCode.SIGN_ERROR);
        }
    }



    /**
     *
     * @param url
     * @param openApiReqVo
     * @param classic
     * @return
     * @param <T>
     * @param <E>
     */
    public <T extends Serializable,E extends Serializable> ApiResult<E> requestUUCommodity(String url, OpenApiReqVo<T> openApiReqVo, Class<E> classic){

        OpenApiReqVo<T> tOpenApiReqVo = requestUUSign(openApiReqVo);
        HttpUtil.HttpRequest.HttpRequestBuilder builder = HttpUtil.HttpRequest.builder();
        builder.url(url);
        builder.method(HttpUtil.Method.JSON);
        builder.postObject(tOpenApiReqVo);
        HttpUtil.HttpResponse sent = HttpUtil.sent(builder.build());
        ApiResult json = sent.json(ApiResult.class);
        Object data = json.getData();
        try {
            ApiResult<E> apiResult=new ApiResult<>();
            E e1 = objectMapper.readValue(objectMapper.writeValueAsString(data), new TypeReference<E>() {});
            apiResult.setData(e1);
            apiResult.setMsg(json.getMsg());
            apiResult.setTimestamp(json.getTimestamp());
            apiResult.setCode(json.getCode());
            return apiResult;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServiceException(-1,"格式转换出错");
        }
    }
}