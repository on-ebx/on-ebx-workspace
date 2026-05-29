#!/bin/bash
# description: Customer Starter Project Setup


# specify the following parameters:
#   1) Full Path to the Starter Project Workspace
#   2) Full Path to where the Target Project Workspace should be created
#   3) The desired name for the Customer's Project in Eclipse  (will replace "company-mdm" in the Starter Project)
#   4) The 4 letter acronym for the Project Domain  (will replace the "DOMN" prefix in the Starter Project)
#   5) The lower case name for the java package node for that Domain (will replace the "domain" package node in the Starter Project)
#   6) The Camel case name for the domain which will be used for Artifact naming that contains the actual Domain Name (will replace the word "Domain" used for Data Model Names, etc in the Starter Project)
#   7) The lower case name for the java package node for that customer (will replace the "company.mdm" package node in the Starter #Project)
#   8) The Camel case name for the Company (will replace the "Company" text in the Starter Project)
#   9) The lower case name for the company (will replace the "company" text in the Starter Project)

# Examples:
# to set permissions from within the folder containing the script:  chmod 755 starter-mvn-project-setup.sh
# ./starter-mvn-project-setup.sh /Users/noiritabera/OrchestraNetworks/Projects/WorkspaceRoot/psworkspace /Users/noiritabera/OrchestraNetworks/Projects/SlingWorkspace sling-mdm TTL title Title sling.mdm Sling sling


echo "*** Setup started ***"

#cd $1
cp -R $1 $2
cd $2
rm -rf .git
mv company-mdm $3
cd $3

echo "* Changes in company-mdm *"

# find and replace string within files recursively
export LC_CTYPE=C
export LANG=C

echo "- Find and replace occurrences of company-mdm with "$3" within the files recursively"
grep -rli 'company-mdm' * | xargs -I@ sed -i '' -e 's/company-mdm/'$3'/g' @

echo "- Find and replace occurrences of DOMN with "$4" within the files recursively"
grep -rli 'DOMN' * | xargs -I@ sed -i '' -e 's/DOMN/'$4'/g' @

echo "- Find and replace occurrences of domain with "$5" within the files recursively"
grep -rli 'domain' * | xargs -I@ sed -i '' -e 's/domain/'$5/'g' @

# replacing domain with customer will also replace the <domain> tags in the Perspective artifact files. The below statement is executed to negate that.
#grep -rli '<customer>' * | xargs -I@ sed -i '' -e 's/<customer>/<domain>/g' @
grep -rli $5'>' * | xargs -I@ sed -i '' -e 's/'$5'>/domain>/g' @

echo "- Find and replace occurrences of Domain with "$6" within the files recursively"
grep -rli 'Domain' * | xargs -I@ sed -i '' -e 's/Domain/'$6'/g' @

echo "- Find and replace occurrences of company.mdm with "$7" within the files recursively. This is for the package structure defined within the java files"
grep -rli 'company.mdm' * | xargs -I@ sed -i '' -e 's/company.mdm/'$7'/g' @

echo "- Find and replace occurrences of Company with "$8" within the files recursively. This is for CompanyDevArtifactsServiceMain and CompanyModuleRegistrationListener class names within the java files"
grep -rli 'Company' * | xargs -I@ sed -i '' -e 's/Company/'$8'/g' @


# rename the file names
echo "- Find and replace occurrences of Company with "$8" in the file names"
find . -name '*Company*' -exec bash -c 'mv $0 ${0/Company/'$8'}' {} \;

echo "- Find and replace occurrences of Domain with "$6" in the file names"
find . -name '*Domain*' -exec bash -c 'mv $0 ${0/Domain/'$6'}' {} \;

echo "- Find and replace occurrences of domain with "$5" in the file names"
find . -name '*domain*' -exec bash -c 'mv $0 ${0/domain/'$5'}' {} \;

echo "- Find and replace occurrences of DOMN with "$4" in the file names"
find . -name '*DOMN*' -exec bash -c 'mv $0 ${0/DOMN/'$4'}' {} \;


# rename folder names
echo "- Find and replace occurrences of company with "$9" in the folder names"
find . -depth -type d -name 'company' -execdir mv {} ''$9'' \;

echo "- Find and replace occurrences of domain with "$5" in the folder names"
find . -depth -type d -name 'domain' -execdir mv {} ''$5'' \;

# rename company-mdm in the .project file
echo "- Rename company-mdm in the .project file with "$3
sed -i '' -e 's/company-mdm/'$3'/g' .project


# Changes in ebx-server
echo "* Changes in ebx-server *"
cd ../ebx-server/
echo "- Find and replace occurrences of company-mdm with "$3" within the files recursively"
grep -rli 'company-mdm' * | xargs -I@ sed -i '' -e 's/company-mdm/'$3'/g' @

# Changes in ebx-home
echo "* Changes in ebx-home *"
cd ../ebx-home/
echo "- Delete the existing h2 repository from ebx-home"
rm -rf ebxRepository/h2/repository.h2.db

# Changes in tools/startscripts
echo "* Changes in tools/startscripts *"
cd ../tools/startscripts/
echo "- Find and replace occurrences of company-mdm with "$3" within the files recursively"
grep -rli 'company-mdm' * | xargs -I@ sed -i '' -e 's/company-mdm/'$3'/g' @

# Changes in ebx-parent
echo "* Changes in ebx-parent *"
cd ../../ebx-parent/
echo "- Find and replace occurrences of company-mdm with "$3" within the files recursively"
grep -rli 'company-mdm' * | xargs -I@ sed -i '' -e 's/company-mdm/'$3'/g' @

# Changes in docker-deploy-command file and dockerfile
echo "* Changes in dockerfile *"
cd ../tools/
echo "- Find and replace occurrences of company-mdm with "$3" within the docker-deploy-command file and dockerfile"
grep -rli 'company-mdm' * | xargs -I@ sed -i '' -e 's/company-mdm/'$3'/g' dockerfile
grep -rli 'company-mdm' * | xargs -I@ sed -i '' -e 's/company-mdm/'$3'/g' 4-docker-deploy.command
chmod 755 dockerfile 4-docker-deploy.command

echo "*** Setup completed ***"
exit 0
