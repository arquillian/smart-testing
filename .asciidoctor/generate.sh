#!/usr/bin/env bash

DELETE=0

while test $# -gt 0; do
       case "$1" in
            -h|--help)
                echo "Builds documentation using asciidoc"
                echo "options:"
                echo "-h, --help                show brief help"
                echo "-k, --keep                keeps used container (useful for travis to export logs)"
                exit 0
                ;;
            -k|--keep)
                shift
                DELETE=1
                shift
                ;;
            *)
               echo "$1 is not a recognized flag!"
               exit -1
               ;;
      esac
done

WORKING_DIR=${TRAVIS_BUILD_DIR:-${PWD}}

docker run -v ${WORKING_DIR}:/docs/ --name adoc-to-html rochdev/alpine-asciidoctor:mini asciidoctor \
    -r /docs/.asciidoctor/lib/const-inline-macro.rb \
    -r /docs/.asciidoctor/lib/copy-to-clipboard-inline-macro.rb \
    -a generated-doc=true -a asciidoctor-source=/docs/docs \
    /docs/README.adoc -o /docs/gh-pages/index.html;

if [ ${DELETE} -eq 0 ]; then
    docker rm adoc-to-html;
fi
