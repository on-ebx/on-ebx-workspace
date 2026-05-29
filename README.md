# Starter eclipse workspace to get started with ON EBX

The puurpose of this project is to get people who are interested in EBX to get started with it in less than 30 mins. Ensure you have access to EBX binaries. If not, reach out to us: contact@onebx.com



## Authors

- [@neel-on-ebx](https://github.com/neel-on-ebx)


## Documentation

[Documentation](https://docs.tibco.com/pub/ebx/latest/doc/html/en/index.html)


## Run Locally

1. Clone the project
2. copy/overwrite the ebx.jar and add-on jar under ebx\ebx.software\lib\
3. copy/overwrite the ebx wars under ebx-server\webapps
4. add your license key to ebx-license.properties under ebx-home. This is provided to you from your account administrator. Ask them to contact us if you don't have one (see contact info at the bottom of this file). 
5. If using EBX version earlier than 6.2.3, copy/overwrite the ebx-lz4.jar under ebx-server\compress. For versions 6.2.3+, you can skip this step.
6. run "ebx-parent" maven config under run-config

Once step 6 shows "BUILD SUCCESSFUL", you may start the ebx-server and configure it.

If you have custom java projects that your module depends upon, you may add them to the pom.xml under ebx-parent and setup similar to how ps-mdm is configured in this workspace.

Thanks,
ON EBX team

Reach out at support@onebx.com / contact@onebx.com for further questions/clarifications
