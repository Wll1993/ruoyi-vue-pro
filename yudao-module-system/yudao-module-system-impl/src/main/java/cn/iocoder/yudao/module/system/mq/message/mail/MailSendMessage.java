package cn.iocoder.yudao.module.system.mq.message.mail;

import cn.iocoder.yudao.framework.mq.core.stream.AbstractStreamMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 邮箱发送消息
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MailSendMessage extends AbstractStreamMessage {

    /**
     * 日志id
     */
    @NotNull(message = "邮箱日志id不能为空")
    private Long logId;
    /**
     * 邮箱地址
     */
    @NotNull(message = "邮箱地址不能为空")
    private String from;
    /**
     * 邮箱模板编号
     */
    @NotNull(message = "邮箱模板编号不能为空")
    private String templateCode;
    /**
     * 收件人
     */
    @NotNull(message = "收件人不能为空")
    private List<String> tos;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;

    @Override
    public String getStreamKey() {
        return "system.mail.send";
    }

}
