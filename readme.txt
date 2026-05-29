Hi,

The EBX binaries will need to be downloaded using the TIBCO support portal account or https://www.tibco.com/downloads. This workspace has the structure to get you started.

1. Download and extract it into a directory so that the eclipse workspace path becomes C:\tibco\ebx\62\cs-workspace. Notice that the ebx-parent directory should be right inside cs-workspace (no other directory). You can have a different path too but in that case you may have to import the projects manually from their location.
2. copy/overwrite the ebx and add-on jars under cs-workspace\ebx\ebx.software\lib\
3. copy/overwrite the product wars under cs-workspace\ebx-server\webapps
4. copy/overwrite the ebx-lz4.jar under cs-workspace\ebx-server\compress
5. run "ebx-parent" maven config under run-config

Once step 5 shows "BUILD SUCCESSFUL", you may start the ebx-server and configure it.

Refactor cs-workspace from company-mdm to your custom module name, as needed. In that case, you will need to update below line in the ebx-server/conf/server.xml as well.
<Context path="/company-mdm" docBase="../../ebx-parent/company-mdm/src/main/webapp"/>

If you have custom java projects that your module depends upon, you may add them to the pom.xml under ebx-parent and setup similar to how ps-mdm is configured in this workspace.

Please note that ps-mdm and attached code is for accelerated development purposes and since this is not part of the product, TIBCO Support cannot provide additional support on this. You can reach out to me for any queries on this workspace, and I will try to help based on my bandwidth.

Thanks,
Neel