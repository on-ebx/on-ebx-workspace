#!/bin/sh



# This script opens a terminal, for customizing the psworkspace



tools_loc="$(cd "$(dirname "${0}")" && pwd)"
outside_workspace_loc="${tools_loc}/../.."
cp starter-mvn-project-setup.sh ${outside_workspace_loc}/starter-mvn-project-setup.sh
cd "${outside_workspace_loc}"
echo "This script is getting executed from: "
pwd
echo "Follwong files and folders are available in this location: " 
ls -lrt
chmod 755 starter-mvn-project-setup.sh
echo "Changed permission for the starter-mvn-project-setup: "
ls -lrt

echo "Example command: ./starter-mvn-project-setup.sh /Users/noiritabera/OrchestraNetworks/Projects/WorkspaceRoot/psworkspace /Users/noiritabera/OrchestraNetworks/Projects/SlingWorkspace sling-mdm TTL title Title sling.mdm Sling sling"
read -p "1) Full Path to the Starter Project Workspace: "  psworkspace_path
read -p "2) Full Path to where the Target Project Workspace should be created: "  custom_workspace_path
read -p "3) The desired name for the Customer's Project in Eclipse  (will replace "company-mdm" in the Starter Project): "  companymdm
read -p "4) The 4 letter acronym for the Project Domain  (will replace the "DOMN" prefix in the Starter Project): "  DOMN
read -p "5) The lower case name for the java package node for that Domain (will replace the "domain" package node in the Starter Project): "  domain
read -p "6) The Camel case name for the domain which will be used for Artifact naming that contains the actual Domain Name (will replace the word "Domain" used for Data Model Names, etc in the Starter Project): "  Domain
read -p "7) The lower case name for the java package node for that customer (will replace the "company.mdm" package node in the Starter #Project): "  companydotmdm
read -p "8) The Camel case name for the Company (will replace the "Company" text in the Starter Project): "  Company
read -p "9) The lower case name for the company (will replace the "company" text in the Starter Project): "  company



./starter-mvn-project-setup.sh $psworkspace_path $custom_workspace_path $companymdm $DOMN $domain $Domain $companydotmdm $Company $company

clear

sh
