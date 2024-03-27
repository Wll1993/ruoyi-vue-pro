package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 仿钉钉的流程器设计器的模型节点类型
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmSimpleModelNodeType implements IntArrayValuable {

    START_NODE(-1, "开始节点"),
    START_USER_NODE(0, "发起人结点"),
    APPROVE_USER_NODE (1, "审批人节点"),
    EXCLUSIVE_GATEWAY_NODE(4, "排他网关"),
    END_NODE(-2, "结束节点");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BpmSimpleModelNodeType::getType).toArray();

    private final Integer type;
    private final String name;

    public static boolean isGatewayNode(Integer type) {
        // TODO 后续增加并行网关的支持
        return Objects.equals(EXCLUSIVE_GATEWAY_NODE.getType(), type);
    }

    public static BpmSimpleModelNodeType valueOf(Integer type) {
        return ArrayUtil.firstMatch(nodeType -> nodeType.getType().equals(type), values());
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }
}