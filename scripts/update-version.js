import fs from "fs";

const path = "src/main/java/dev/x341/aonbas2srv/util/AOBConstants.java";
const version = process.env.RELEASE_VERSION;

let content = fs.readFileSync(path, "utf8");
content = content.replace(/VERSION_BUILD = \d+;/, `VERSION_BUILD = ${version.split(".")[2]};`);
fs.writeFileSync(path, content);
