/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ssehub.kernel_haven.non_boolean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;

// TODO: tidy up this temporary hack
// CHECKSTYLE:OFF

public abstract class PreprocessorConditionVisitor {
    
    public void visitAllFiles(File directory) throws IOException {
        try {
            Files.walk(directory.toPath(), FileVisitOption.FOLLOW_LINKS).forEach((file) -> {
                try {
                    visiFile(file.toFile());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    public void visiFile(File file) throws IOException {
        if (!file.isFile() || (!file.getName().endsWith(".c") && !file.getName().endsWith(".h"))) {
            return;
        }
        
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                
                if (!CPPUtils.isIfOrElifStatement(line)) {
                    continue;
                }
                
                // Consider continuation
                while (line.charAt(line.length() - 1) == '\\') {
                    String tmp = in.readLine();
                    if (null != tmp) {
                        line += tmp;
                    } else {
                        break;
                    }
                }
                
                visit(file, line);
            }
            
        }
    }
    
    public abstract void visit(File file, String line);
    
}
