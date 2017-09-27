#!/usr/bin/env bash

MAVEN_METADATA=$(curl -sL http://central.maven.org/maven2/org/arquillian/smart/testing/smart-testing-parent/maven-metadata.xml)
LATEST=$(echo ${MAVEN_METADATA} | grep '<latest>' | sed -e 's,.*<latest>\([^<]*\)</latest>.*,\1,g')

VERSION=${LATEST}
INSTALL_SPECIFIC_VERSION="0"

while test $# -gt 0; do
       case "$1" in
            -h|--help)
                echo "Installs Arquillian Smart Testing Extension"
                echo "options:"
                echo "-l, --latest                   installs latest version of the extension (default)"
                echo "-v, --version=VERSION          installs defined version (doesn't check if exists!)"
                exit 0
                ;;
            -l|--latest)
                shift
                VERSION=${LATEST}
                shift
                ;;
            -v)
                shift
                if test $# -gt 0; then
                    VERSION=$1
                    INSTALL_SPECIFIC_VERSION="1"
                else
                    echo "No version specified."
                    exit 1
                fi
                shift
                ;;
            --version*)
                VERSION=`echo $1 | sed -e 's/^[^=]*=//g'`
                INSTALL_SPECIFIC_VERSION="1"
                shift
                ;;
            *)
               echo "$1 is not a recognized flag!"
               exit -1
               ;;
      esac
done

function install_shaded_library() {
    if [ -z "$M2_HOME" ]; then
        echo "Please set M2_HOME pointing to your Maven installation."
        exit 1
    fi
    SHADED_JAR="maven-lifecycle-extension-${VERSION}-shaded.jar"
    echo "Installing ${SHADED_JAR} into ${M2_HOME}/lib/ext"
    wget http://central.maven.org/maven2/org/arquillian/smart/testing/maven-lifecycle-extension/${VERSION}/${SHADED_JAR}
    echo -n "We want to move shaded jar to M2_HOME with sudo. Can we? [y/N] "
    read -r response < /dev/tty
    case "$response" in
        [yY][eE][sS]|[yY])
              sudo mv $SHADED_JAR $M2_HOME/lib/ext
            ;;
        *)
            ;;
    esac
}

function install_extension() {

    # Needs to be without new lines, otherwise my sed skills below will fail badly :\
    EXTENSION="<extension><groupId>org.arquillian.smart.testing</groupId><artifactId>maven-lifecycle-extension</artifactId><version>${VERSION}</version></extension>"

    [ -d .mvn ] || mkdir .mvn

    if [ ! -f .mvn/extensions.xml ]; then
        echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
            <extensions>
              ${EXTENSION}
            </extensions>" > .mvn/extensions.xml
    else
        EXTENSION_REGISTERED=$(cat .mvn/extensions.xml | grep 'org.arquillian.smart.testing' -A 2 |  grep '<version>' | sed -e 's,.*<version>\([^<]*\)</version>.*,\1,g');

        if [ ! ${EXTENSION_REGISTERED} ]; then
            EXTENSION=$(echo ${EXTENSION} | sed -e "s#/#\\\/#g");
            sed -i -E 's/(.*<extensions>)(.*)/\1\n'$EXTENSION'\2/g' .mvn/extensions.xml
            echo "Installed Smart Testing Extension ${VERSION}"
        else
            echo -e "Smart Testing Extension already registered with version ${EXTENSION_REGISTERED}\c"

            if [ ${INSTALL_SPECIFIC_VERSION} -eq 1 ]; then
                if [ "${EXTENSION_REGISTERED}" != "${VERSION}" ]; then
                    echo -e " - overwriting with ${VERSION}."
                    override_version $VERSION
                    echo -e "Updated Smart Testing Extension to ${VERSION}\c"
                fi
                echo "."
            elif [ $EXTENSION_REGISTERED != $LATEST ]; then
                echo -n ". Do you want to override with latest ${LATEST}? [y/N] "
                read -r response < /dev/tty
                case "$response" in
                    [yY][eE][sS]|[yY])
                          override_version ${LATEST}
                          echo "Updated Smart Testing Extension to ${LATEST}"
                        ;;
                    *)

                        ;;
                esac
            else
                echo " which is the latest stable version."
            fi
        fi
    fi

    mv .mvn/extensions.xml .mvn/extensions-unformatted.xml
    xmllint --format .mvn/extensions-unformatted.xml > .mvn/extensions.xml
    rm .mvn/extensions-unformatted.xml
}

function override_version() {

    echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>
<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">

    <xsl:param name=\"version\"/>
    <xsl:template match=\"node()|@*\">
        <xsl:copy>
            <xsl:apply-templates select=\"node()|@*\"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match=\"/extensions/extension[groupId='org.arquillian.smart.testing' and artifactId='maven-lifecycle-extension']/version/text()\">
        <xsl:value-of select=\"\$version\"/>
    </xsl:template>

</xsl:stylesheet>
    " >> .mvn/updateversion.xslt
    xsltproc --stringparam version $1 .mvn/updateversion.xslt .mvn/extensions.xml > .mvn/extensions-new.xml
    mv .mvn/extensions-new.xml .mvn/extensions.xml
    rm .mvn/updateversion.xslt
}

function ignore_smart_testing_artifacts() {
    cat .gitignore 2>&1  | grep -q '.smart-testing' && EXISTS=1 || EXISTS=0
    if [ ${EXISTS} == 0 ]; then
        echo -n "Do you want to add Smart Testing execution artifacts to .gitignore? [Y/n] "
        read -r response < /dev/tty
            case "$response" in
                [nN][oO]|[nN])
                    ;;
                *)
                    if [ ! -f .gitignore ]; then
                        touch ./.gitignore
                    fi

                        echo -e "\n# Smart Testing Exclusions\n.smart-testing/\n" >> ./.gitignore
                    ;;
            esac
    fi
}

function command_exists {
  # Sample usage "if command_exists foo; then echo it exists; fi"
  type "$1" &> /dev/null
}

## MAIN LOGIC

command_exists mvn 2>&1 || { echo >&2 "Cannot find Maven (mvn). Make sure you have it installed."; exit 1; }
command_exists xmllint >/dev/null 2>&1 || { echo >&2 "This script requires xmllint. Make sure you have it installed."; exit 1; }
command_exists xsltproc >/dev/null 2>&1 || { echo >&2 "This script requires xsltproc. Make sure you have it installed."; exit 1; }

if [[ ! -f pom.xml ]]; then
  echo >&2 "Cannot find pom.xml file. Is it a Maven project? Make sure you are in the project's root directory."
  exit 1
fi

MVN_VERSION=$(mvn --version | head -n1 | cut -d' ' -f3)

if [[ $MVN_VERSION =~ ^[3].[3-9].[0-9]$ ]]; then
    echo "Installing extension in .mvn/extensions.xml"
    install_extension
    ignore_smart_testing_artifacts
elif [[ $MVN_VERSION =~ ^[3].[1-2].[0-9]$ ]]; then
    echo "Installing extension in M2_HOME/lib/ext"
    install_shaded_library
    ignore_smart_testing_artifacts
else
    echo "Version ${MVN_VERSION} is not supported.";
fi

