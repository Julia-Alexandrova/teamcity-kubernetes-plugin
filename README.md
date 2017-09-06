# TeamCity Kubernetes Support Plugin
[![plugin status]( 
https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_TeamCityKubernetesPlugin_Build)/statusIcon.svg)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_TeamCityKubernetesPlugin_Build&guest=1)

Run TeamCity cloud agents on Kubernetes cluster.

## Compatibility

The plugin is compatible with TeamCity 10.0.x and greater.

## Installation

You can [download the plugin](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_TeamCityKubernetesPlugin_Build/lastSuccessful/teamcity-kubernetes-plugin.zip) and install it as an [additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

## Configuration

The plugin supports Kubernetes cluster images to start new pods with a TeamCity build agent running in one of the containers. The plugin supports the [official TeamCity Build Agent Docker image](https://hub.docker.com/r/jetbrains/teamcity-agent) out of the box. You can use your own image as well.

## License

Apache 2.0

## Feedback

Please feel free to post feedback in the repository issues.
