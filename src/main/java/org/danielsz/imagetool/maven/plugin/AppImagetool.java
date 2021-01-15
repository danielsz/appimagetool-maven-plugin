package org.danielsz.imagetool.maven.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import clojure.java.api.Clojure;
import clojure.lang.IFn;

import java.util.concurrent.Callable;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "appimagetool", defaultPhase = LifecyclePhase.PACKAGE)
public class AppImagetool extends AbstractMojo {

    /**
     * Where we are going to create the AppImage.
     */
    @Parameter(defaultValue = "${project.build.directory}/imagetool", property = "imagetool.outputDirectory", required = true)
    private File outputDirectory;
    
    /**
     * Where we are going to read the output of jpackage.
     */
    @Parameter(defaultValue = "${project.build.directory}/jpackage", property = "imagetool.inputDirectory", required = true)
    private File inputDirectory;
    
    /**
     *
     * @throws MojoExecutionException
     */
    @Override
    public void execute() throws MojoExecutionException {
        getLog().info( "input directory" + inputDirectory);
        getLog().info( "output directory" + outputDirectory);

	IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("imagetool.imagetool"));

	IFn jpackageToImagetool = Clojure.var("imagetool.imagetool", "jpackage->imagetool");
	jpackageToImagetool.invoke(inputDirectory, outputDirectory);			   
     }
}
