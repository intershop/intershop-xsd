# Intershop XML Schema Definitions

[![Latest release](https://badgen.net/github/release/intershop/intershop-xsd/stable)](https://github.com/intershop/intershop-xsd/releases)
[![License](https://badgen.net/github/license/intershop/intershop-xsd)](LICENSE.md)
[![Automated checks](https://badgen.net/github/checks/intershop/intershop-xsd)](https://github.com/intershop/intershop-xsd/actions)
[![Dependabot](https://badgen.net/github/dependabot/intershop/intershop-xsd)](.github/dependabot.yml)

This project provides XML Schema Definitions (XSDs) of Intershop packaged as JAR. It will also fetch and provide external referenced XSDs to avoid the necessity to have internet access during various processing steps.


# Setup
To clone this project with its Git submodule _schemas_, you want to clone this repository recursively with `--recursive`:

```shell
git clone --recursive https://github.com/intershop/intershop-xsd.git
```

# Submodules
| submodule | repository                                 |
|-----------|--------------------------------------------|
| _schemas_ | https://github.com/intershop/intershop-xsd |

To avoid merge conflicts, make a separate commit for submodule updates, so you can drop that commit afterward.


# Examples
To find examples of schemas in use, you might want to take a look into the [examples](examples) directory.


# Third party XSD
Third party XSDs are retrieved during build and for publishing to store all reference XSDs in one artifact.

| file location              | target namespace            |
|----------------------------|-----------------------------|
| xml/ns/www.w3.org/XML/1998 | http://www.w3.org/XML/1998/ |
| xml/ns/www.w3.org/2001     | http://www.w3.org/2001/     |


# Pull Requests
After having your XSD changes merged in the [intershop.github.io repository](https://github.com/intershop/intershop.github.io),
go to the Git submodule _schemas_ directory and update the submodule to the remote repository commit you want to use for your PR and release of this artifact afterwards.


# Release
Create a new release in this project with the corresponding new version according to semantic versioning.
The referenced commit from the Git submodule _schemas_ will be checked out during release creation.


# License
Copyright 2023 Intershop Communications.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
