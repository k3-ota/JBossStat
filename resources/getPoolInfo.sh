#/bin/sh
/opt/jboss-eap-6.2/bin/jboss-cli.sh -c --commands="cd /subsystem=datasources/data-source=${1}/statistics=pool/,ls"
