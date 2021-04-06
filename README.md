NPM/Yarn Wrapper and Steps Plugin
=================================

This is a pipeline-friendly Jenkins plugin that provides an npm wrapper, a yarn wrapper and npm and yarn build steps for
Unix systems. If NodeJS and/or Yarn are not available, it installs them on demand
using [nvm](https://github.com/nvm-sh/nvm) and
the [yarn classic installation script](https://classic.yarnpkg.com/en/docs/install). If there is an .nvmrc file in the
directory where the commands are to be executed, it respects the version specified there. Otherwise, it uses the latest
stable version of NodeJS. The wrapper allows specifying a workspace subdirectory (to provide an .npmrc file) and
providing login credentials.

## NPM Credentials

Users can provide credentials for a private or corporate NPM repository by setting them up through the custom NPM
Credential type. To do so, navigate to manage credentials within Jenkins and provide the appropriate url, username, user
email and password.

![Select NPM Login Credentials as the credential kind.](images/credentials-1.png?raw=true "Select NPM Login Credentials")

Fill in the form with the correct credentials for your Artifactory. Note that currently _no_ validation is done to
ensure that the credentials are correct prior to storing them.

![Fill in the appropriate login information.](images/credentials-2.png?raw=true "Provide the correct credentials")

## Wrapper in Freestyle Projects

To set up the wrapper, select an NPM Login Credential and, optionally, set a subdirectory of the workspace to use when
setting up NodeJS. The primary reason to add a workspace subdirectory to the wrapper is if you're using an .nvmrc file
to manage your NodeJS version.

![Select a credential and provide a workspace subdirectory](images/freestyle-wrapper-1.png?raw=true "Select a credential")

## Steps in Freestyle Projects

### NPM Step
![Select a credential and provide a workspace subdirectory](images/freestyle-npm-build-step.png?raw=true "Select a credential")

### Yarn Step

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
