#!/bin/bash
# Deploy maven artifact in current directory into Maven central repository
# using maven-release-plugin goals



###########################################################
###########################################################
cat << "RELEASE"
██████╗ ███████╗██╗     ███████╗ █████╗ ███████╗███████╗
██╔══██╗██╔════╝██║     ██╔════╝██╔══██╗██╔════╝██╔════╝
██████╔╝█████╗  ██║     █████╗  ███████║███████╗█████╗
██╔══██╗██╔══╝  ██║     ██╔══╝  ██╔══██║╚════██║██╔══╝
██║  ██║███████╗███████╗███████╗██║  ██║███████║███████╗
╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝╚═╝  ╚═╝╚══════╝╚══════╝
RELEASE

set -e

mvn --settings ~/.m2/settings.xml scm:check-local-modification

# release
echo "\n----------------------------------------"
echo "When at prompt, type the release version (e.g. from 1.0-SNAPSHOT to 1.0)\n"
mvn --settings ~/.m2/settings.xml versions:set
git commit -am "[Deploy phase] Preparing release"
mvn --settings ~/.m2/settings.xml clean deploy -DskipTests -P release
mvn --settings ~/.m2/settings.xml scm:tag
echo 'Release deployed'


###########################################################
###########################################################
# next development version
cat << "NEXT_ITERATION"
███╗   ██╗███████╗██╗  ██╗████████╗
████╗  ██║██╔════╝╚██╗██╔╝╚══██╔══╝
██╔██╗ ██║█████╗   ╚███╔╝    ██║
██║╚██╗██║██╔══╝   ██╔██╗    ██║
██║ ╚████║███████╗██╔╝ ██╗   ██║
╚═╝  ╚═══╝╚══════╝╚═╝  ╚═╝   ╚═╝

    ██╗████████╗███████╗██████╗  █████╗ ████████╗██╗ ██████╗ ███╗   ██╗
    ██║╚══██╔══╝██╔════╝██╔══██╗██╔══██╗╚══██╔══╝██║██╔═══██╗████╗  ██║
    ██║   ██║   █████╗  ██████╔╝███████║   ██║   ██║██║   ██║██╔██╗ ██║
    ██║   ██║   ██╔══╝  ██╔══██╗██╔══██║   ██║   ██║██║   ██║██║╚██╗██║
    ██║   ██║   ███████╗██║  ██║██║  ██║   ██║   ██║╚██████╔╝██║ ╚████║
    ╚═╝   ╚═╝   ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝   ╚═╝ ╚═════╝ ╚═╝  ╚═══╝
NEXT_ITERATION

echo "When at prompt, type the SNAPSHOT version (e.g. from 1.0 to 2.0-SNAPSHOT)\n"
mvn --settings ~/.m2/settings.xml versions:set
git commit -am "[Deploy phase] Preparing for next iteration"

# updating origin
git push --force --follow-tags

echo 'Next iteration prepared'
