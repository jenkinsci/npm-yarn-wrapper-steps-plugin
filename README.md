NPM/Yarn Wrapper and Steps Plugin
=================================

This is a pipeline-friendly Jenkins plugin that provides an npm wrapper, a yarn wrapper and npm and yarn build steps for
Unix systems. If NodeJS and/or Yarn are not available, it installs them on demand
using [nvm](https://github.com/nvm-sh/nvm) and
the [yarn classic installation script](https://classic.yarnpkg.com/en/docs/install). If there is an .nvmrc file in the
directory where the commands are to be executed, it respects the version specified there. Otherwise, it uses the latest
stable version of NodeJS. The wrappers allow setting custom repositories, specifying an override NodeJS version and
providing login credentials.

## Steps in Pipelines

This plugin provides steps for use in Jenkins Pipelines. They will install [nvm](https://github.com/nvm-sh/nvm) if not
already installed and will use the version of NodeJS specified in the project's `.nvmrc` file. The steps accept a raw
command to passed to `yarn` or `npm run` and, optionally, a subdirectory of the workspace from which to execute the
command. If no subdirectory is provided, the steps assume the root of the workspace.

```groovy
pipeline {
    agent any
    stages {
        stage('Hello') {
            steps {
                yarn 'init -y'
                npm 'view react'
            }
        }
        stage('Hello with subdirectories') {
            steps {
                yarn command: 'init -y', workspaceSubdirectory 'some-subdirectory'
                npm command: 'view react', workspaceSubdirectory 'some-other-subdirectory'
            }
        }
    }
}
```

## Wrapper in Pipelines

The plugin includes a special credential type for npm, which is used in the wrapper to support private npm repositories.

```groovy
pipeline {
    agent any
    stages {
        stage('Hello') {
            steps {
                withNPMWrapper('MyCredential') {
                    npm 'init -y'
                    npm command: 'publish'
                }
            }
        }
    }
}
```
