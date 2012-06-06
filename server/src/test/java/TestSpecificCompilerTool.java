/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.avro.tool.SpecificCompilerTool;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;

/**
 * Verifies that the SpecificCompilerTool generates Java source properly
 */
public class TestSpecificCompilerTool {

    @Test
    public void testGenerate() throws Exception {
        getAvroDir();

        final String inputDir = getAvroDir();
        final String outputDir = getSrcDir();

        File _inputDir = new File(inputDir);
        File _outputDir = new File(outputDir);
        if (!_inputDir.exists()) {
            throw new IOException("Dir " + inputDir + " is not exists.");
        }

        File[] files = _inputDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("avro");
            }
        });

        if (files.length == 0) {
            return;
        }

        if (!_outputDir.exists()) {
            _outputDir.mkdirs();
        }
        String[] params = new String[files.length + 2];

        params[0] = "protocol";
        for (int i = 1; i < params.length - 1; i++) {
            params[i] = files[i - 1].getAbsolutePath();
        }
        params[params.length - 1] = outputDir;
        doCompile(params);
    }

    private String getAvroDir() {
        String path = getClass().getResource("").getPath();
        int index = path.indexOf("/target");
        String base = path.substring(0, index);
        return base + "/src/main/resources/avro";
    }

    private String getSrcDir() {
        String path = getClass().getResource("").getPath();
        int index = path.indexOf("/target");
        String base = path.substring(0, index);
        return base + "/src/main/java";
    }

    // Runs the actual compiler tool with the given input args
    private void doCompile(String[] args) throws Exception {
        SpecificCompilerTool tool = new SpecificCompilerTool();
        tool.run(null, null, null, Arrays.asList((args)));
    }
}
