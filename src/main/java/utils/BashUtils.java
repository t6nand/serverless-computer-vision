package main.java.utils;

import com.tinkerpop.pipes.util.structures.Pair;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by tapansharma on 12/10/17.
 */
public class BashUtils {
    private static final int BASH_COMMAND_TIMEOUT = 15 * 60;

    /**
     * The Bash Command output is ignored which might be necessary if the command gives some console output.
     *
     * @param command The Bash command to run.
     * @return true if the command runs successfully.
     */
    public static boolean runBashCommand0(String command) {
        boolean commandRanSuccessfully;
        try {
            runBashCommand(command);
            commandRanSuccessfully = true;
        } catch (Exception e) {
            commandRanSuccessfully = false;
            System.out.println("Bash Command execution failure: " + e);
        }
        return commandRanSuccessfully;
    }

    /**
     * @param command The Bash command to run.
     * @return the console output from the command.
     */
    public static String runBashCommand(String command) throws Exception {
        try {
            Pair<Integer, String> cmdRet = getCmdResult(command);
            if (cmdRet.getA() != 0) {
                return cmdRet.getB();
                //throw new Exception("Command " + command + " returned with failure status " + cmdRet.getA());
            } else {
                return cmdRet.getB();
            }
        } catch (Exception e) {
            throw new Exception("Could not execute command: " + command, e);
        }
    }

    /**
     * Get the result of a system command. Any output from command over 4 KB will cause the waitFor call to hang. The
     * output can be either from InputStream or OutputStream.
     *
     * @param command The command to execute
     * @return Pair of exit status and message returned by command
     * @throws IOException
     * @throws InterruptedException
     */
    private static Pair<Integer, String> getCmdResult(String command)
            throws IOException, InterruptedException {
        System.out.println("Running image command: " + command);
        String[] cmdArr = {"/bin/bash", "-c", command};
        Process cmd = Runtime.getRuntime().exec(cmdArr);

        String result;
        {
            // ToDo: The problem here is if any data is written to error stream over 4 KB, it will cause app to hang.
            // Read Input Stream
            BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
            String tmp = reader.readLine();
            StringBuilder outputBuilder = new StringBuilder();
            while (tmp != null) {
                outputBuilder.append(tmp).append(System.lineSeparator());
                tmp = reader.readLine();
            }
            result = outputBuilder.toString();
            reader.close();
        }

        String err;
        {
            // Read Error Stream
            BufferedReader errReader = new BufferedReader(new InputStreamReader(cmd.getErrorStream()));
            err = errReader.readLine();
            StringBuilder errorBuilder = new StringBuilder();
            while (null != err) {
                errorBuilder.append(err).append(System.lineSeparator());
                err = errReader.readLine();
            }
            errReader.close();

            // Do not always log, since a lot of library writes additional info(webp, jpeg-archive) to this stream.
            err = errorBuilder.toString();
        }

        int exitValue = cmd.waitFor();
        if (StringUtils.isNotBlank(err)) {
            if (exitValue != 0) {
                System.out.println("CMD_ERR: {}" + err);
            } else {
                System.out.println("CMD_ERR: {}" + err);
            }
        }
        return new Pair<>(exitValue, result);
    }
}
