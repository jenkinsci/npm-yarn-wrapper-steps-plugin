NPM/Yarn Wrapper and Steps Plugin
=================================

This is a pipeline-friendly Jenkins plugin that provides an npm wrapper, a yarn wrapper and npm and yarn build steps for Unix systems. If NodeJS and/or Yarn are not available, it installs them on demand using [nvm](https://github.com/nvm-sh/nvm) and the [yarn classic installation script](https://classic.yarnpkg.com/en/docs/install). If there is an .nvmrc file in the directory where the commands are to be executed, it respects the version specified there. Otherwise, it uses the latest stable version of NodeJS. The wrappers allow setting custom repositories, specifying an override NodeJS version and providing login credentials.


