/* This file is part of OctTreeRayTracer.
 *
 * OctTreeRayTracer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OctTreeRayTracer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OctTreeRayTracer.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.scalajs.core.tools.sem.Semantics.RuntimeClassNameMapper
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

// TO TEST RUN
//   - otrtJVM/test
//   - otrtJS/test

lazy val otrt = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in( file(".") )
  .settings(
     name := "otrt",
     version := "0.1.0",
     scalaVersion := "2.12.8",
     scalacOptions ++= Seq(
       "-feature",
       "-deprecation"
     ),

     testFrameworks += new TestFramework("utest.runner.Framework"),

     libraryDependencies ++= Seq(
       "com.lihaoyi"   %%% "utest" % "0.6.6" % "test",
       "org.typelevel" %%% "spire" % "0.16.0"
     )
   )
  .jsSettings(
     mainClass in Compile := Some("otrt.MainJS"),
     scalaJSUseMainModuleInitializer := true,
     scalaJSStage := FullOptStage,
     scalaJSSemantics ~= (
       _ withStrictFloats false
         withRuntimeClassNameMapper RuntimeClassNameMapper.discardAll()
     ),
     libraryDependencies ++= Seq(
       "org.scala-js" %%% "scalajs-dom" % "0.9.6"
     )
   )
  .jvmSettings(
     mainClass := Some("otrt.MainJVM"),
   )

lazy val otrtJS = otrt.js
lazy val otrtJVM= otrt.jvm

//lazy val deltriBuild = project.in( file(".") )
//  .aggregate(deltriJS, deltriJVM)
//  .settings()
