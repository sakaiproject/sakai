import micromatch from 'micromatch'

export default allStagedFiles => {

  const codeFiles = micromatch(allStagedFiles, ['**/src/*.js']);
  const basePath = "webcomponents/tool/src/main/frontend/node_modules/.bin/";
  const analyzerOptions = "--strict --quiet --rules.no-complex-attribute-binding=warning --rules.no-incompatible-type-binding=off";
  return [
    `${basePath}/eslint --fix ${codeFiles.join(' ')}`,
    `${basePath}/lit-analyzer ${analyzerOptions} ${codeFiles.join(' ')}`
  ];
};
