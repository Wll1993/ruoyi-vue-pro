package cn.iocoder.yudao.module.infra.service.codegen.inner.generator;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.enums.codegen.MockTypeEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 系统词典数据生成器
 *
 * 利用curl命令去获取词典
 */
@Slf4j
public class DictDataGenerator implements DataGenerator {
    private final ObjectMapper objectMapper;

    public DictDataGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public int getOrder() {
        return MockTypeEnum.DICT.getType();
    }

    /**
     * 执行指令，例如
     * “curl -X GET -H  "Accept:***" -H  "X-Access-Token:xxx" -H  "Content-Type:application/x-www-form-urlencoded" "https://xxx"”
     *
     * @param commandFile 任意命令
     * @return 执行命令的进程
     * @throws IOException IO出现异常
     */
    private Process buildProcess(String commandFile) throws IOException {
        // setting up user to run commands
        List<String> command = new LinkedList<>();

        // init process builder
        ProcessBuilder processBuilder = new ProcessBuilder();
        // merge error information to standard output stream
        processBuilder.redirectErrorStream(true);
        command.add(commandInterpreter());
        command.addAll(Collections.emptyList());
        command.add(commandFile);

        // setting commands
        processBuilder.command(command);
        Process process = processBuilder.start();

        printCommand(command);
        return process;
    }

    /**
     * 处理命令的输出
     *
     * @param process 执行命令的进程
     * @return 命令执行过程的全部输出
     */
    private String parseProcessOutput(Process process) {
        List<String> logBuffer = new LinkedList<>();
        try (BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = inReader.readLine()) != null) {
                logBuffer.add(line);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return StrUtil.join(StrUtil.EMPTY, logBuffer);
    }

    /**
     * 输出命令是什么
     *
     * @param commands
     */
    private void printCommand(List<String> commands) {
        log.info("[codegen-fake-data]run command: {}", String.join(" ", commands));
    }

    /**
     * 不同环境的命令执行器
     *
     * @return 执行器
     */
    private String commandInterpreter() {
        return SystemUtils.IS_OS_WINDOWS_10 ? "powershell.exe" :
                SystemUtils.IS_OS_WINDOWS ?
                        "cmd.exe" : "/bin/bash";
    }

    @Override
    public String getName() {
        return MockTypeEnum.DICT.getLabel();
    }

    @Override
    public List<String> doGenerate(CodegenColumnDO field, int rowNum) {
        //curl命令的参数
        String mockParams = field.getMockParams();

        String commandFile = "curl " + mockParams;
        try {
            Process process = buildProcess(commandFile);
            String json = parseProcessOutput(process);
            //期望收到的是一个词组列表
            List<String> wordList = objectMapper.readValue(json,
                    new TypeReference<List<String>>() {
                    });
            //从词组列表中随机
            List<String> list = new ArrayList<>(rowNum);
            for (int i = 0; i < rowNum; i++) {
                String randomStr = wordList.get(RandomUtils.nextInt(0, wordList.size()));
                list.add(randomStr);
            }
            return list;
        } catch (IOException e) {
            log.error("[codegen-fake-data]execute command error", e);
        }
        return Collections.emptyList();
    }
}
