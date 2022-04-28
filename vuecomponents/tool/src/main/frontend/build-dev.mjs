// Helper script to provide a better development expierience
// working on vue components in Sakai

//NEEDS DEBUGGING ON WINDOWS

"use strict";

import { execSync as run } from "child_process";
import { existsSync as exists } from "fs";

// You can set a custom path to your tomcat folder here
let tomcatPath;

//Read TOMCAT_HOME enviornment variable
let tomcatHome = process.env.TOMCAT_HOME;

// Check if TOMCAT_HOME is set, but dont use it if tomcatPath is set already
if (!tomcatPath && tomcatHome && !(tomcatHome.trim().length === 0)) {
  tomcatPath = tomcatHome;
}

if (tomcatPath) {
  //Remove trailing slashes
  tomcatPath = tomcatPath.replace(/\/+$/, "");

  //Define deployment path
  let deploymentPath = tomcatPath + "/webapps/vuecomponents/js";

  //Log deployment path
  console.log(`Deploying vuecomponents to: ${deploymentPath}`);

  //Check if node_modules and vue-cli-service exist
  let nodeModulesExist = exists("node_modules");

  if (!nodeModulesExist) {
    //node modules dont exist - lets install them
    console.log('Node module folder is not present - running "npm install":');
    run("npm install --unsafe-perm", { stdio: "inherit" });
  }

  let vueCliServiceExists = exists("node_modules/.bin/vue-cli-service");

  if (!vueCliServiceExists) {
    console.error("Error: vue cli service not found!");
    process.exit(1);
  }

  //Run build/deploy process
  run(
    `node_modules/.bin/vue-cli-service build --target wc --name sakai --dest ${deploymentPath} --watch './src/components/*'`,
    { stdio: "inherit" }
  );
} else {
  //No TOMCAT_HOME or tomcatPath specified, print error
  console.error(
    "Error: Please specify tomcatPath in build-dev.js or set TOMCAT_HOME enviornment variable"
  );
  process.exit(1);
}
