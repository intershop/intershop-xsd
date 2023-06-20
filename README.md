# Intershop XML Schema Definitions
This project provides XML Schema Definitions (XSDs) of Intershop packaged as JAR. It will also fetch and provide external referenced XSDs to avoid the necessity to have internet access during various processing steps.


# Setup
To clone this project with its Git submodule [schemas](schemas), you want to clone this repository recursively with `--recursive`:

```shell
git clone --recursive https://github.com/IntershopCommunicationsAG/intershop-xsd.git
```

# Submodules
| submodule          | repository                                                 |
|--------------------|------------------------------------------------------------|
| [schemas](schemas) | https://github.com/IntershopCommunicationsAG/intershop-xsd |

To avoid merge conflicts, make a separate commit for submodule updates, so you can drop that commit afterward.


# Third party XSD
Third party XSDs are retrieved during build and for publishing to store all reference XSDs in one artifact.

| file location              | target namespace            |
|----------------------------|-----------------------------|
| xml/ns/www.w3.org/XML/1998 | http://www.w3.org/XML/1998/ |
| xml/ns/www.w3.org/2001     | http://www.w3.org/2001/     |


# Release
After having your XSD changes merged in the [intershop.github.io repository](https://github.com/intershop/intershop.github.io),
just create a new release in this project with the corresponding new version according to semantic versioning.
The branch specific, current referenced commit from the Git submodule [schemas](schemas) will be checked out during release creation.


# License
Copyright 2023 Intershop Communications.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.