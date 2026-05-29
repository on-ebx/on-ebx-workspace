package com.orchestranetworks.ps.rest.directory;

import java.util.*;

import com.orchestranetworks.rest.annotation.*;
import com.orchestranetworks.rest.inject.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.*;

/**
 * Provides Directory related REST Services developed using the APIs offered by the REST Toolkit.
 * 
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path(value = "/directory")
@Documentation("Directory REST Service")
public class DirectoryRESTService
{
	@Context
	private ResourceInfo resourceInfo;

	@Context
	private SessionContext sessionContext;

	@GET
	@Path("/description")
	@Documentation("Gets service description")
	@Consumes({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	@AnonymousAccessEnabled
	// TODO: Should this be an anonymous service?
	public String handleServiceDescription()
	{
		// TODO: Not sure why we're doing this
		return this.resourceInfo.getResourceMethod().getAnnotation(Documentation.class).value();
	}

	/**
	 * Selects users in the given built-in role (administrator, everyone, owner, read-only).
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the service: 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/directory/directory/usersInBuiltInRole/<roleName>
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/directory/directory/usersInBuiltInRole/administrator
	 * }
	 * </pre>
	 * @param roleName the built-in role name (administrator, everyone, owner, read-only).
	 * @return list of {@link UserDTO} with the userId and label.
	 */
	@GET
	@Path("/usersInBuiltInRole/{roleName}")
	@Documentation("Selects users in a built-in role (administrator, everyone, owner, read-only)")
	public Collection<UserDTO> handleGetUsersInBuiltInRole(@PathParam("roleName") String roleName)
	{
		Role role = Role.forBuiltInRole(roleName);
		return doGetUsersInRole(role);
	}

	/**
	 * Selects users in a specific role (not a built-in role).
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the service: 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/directory/directory/usersInSpecificRole/<roleName>
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/directory/directory/usersInSpecificRole/COMM
	 * }
	 * </pre>
	 * @param roleName the specific role name.
	 * @return list of {@link UserDTO} with the userId and label.
	 */
	@GET
	@Path("/usersInSpecificRole/{roleName}")
	@Documentation("Selects users in a specific role (not a built-in role)")
	public Collection<UserDTO> handleGetUsersInSpecificRole(@PathParam("roleName") String roleName)
	{
		Role role = Role.forSpecificRole(roleName);
		return doGetUsersInRole(role);
	}

	/**
	 * Selects all specific roles (not built-in roles).
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the service: 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/directory/directory/allSpecificRoles
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/directory/directory/allSpecificRoles
	 * }
	 * </pre>
	 * @return list of {@link RoleDTO} with the name and label.
	 */
	@GET
	@Path("/allSpecificRoles")
	@Documentation("Get all specific roles (not built-in roles)")
	public Collection<RoleDTO> getAllSpecificRoles()
	{
		Session session = sessionContext.getSession();
		DirectoryHandler dirHandler = session.getDirectory();
		if (dirHandler.getDirectoryImplementation() instanceof DirectoryDefault)
		{
			DirectoryDefault directorydefault = (DirectoryDefault) dirHandler
				.getDirectoryImplementation();
			List<Role> roles = directorydefault.getAllSpecificRoles();
			return createRoleDTOs(roles, session.getLocale());
		}
		return null;
	}

	/**
	 * Selects all users.
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the service: 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/directory/directory/allUsers
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/directory/directory/allUsers
	 * }
	 * </pre>
	 * @return list of {@link UserDTO} with the userId and label.
	 */
	@GET
	@Path("/allUsers")
	@Documentation("Get all users")
	public Collection<UserDTO> getAllUserReferences()
	{
		Session session = sessionContext.getSession();
		DirectoryHandler dirHandler = session.getDirectory();
		DirectoryDefault directorydefault = (DirectoryDefault) dirHandler
			.getDirectoryImplementation();
		List<UserReference> userReferences = directorydefault.getAllUserReferences();
		if (dirHandler.getDirectoryImplementation() instanceof DirectoryDefault)
		{
			return createUserDTOs(
				userReferences,
				dirHandler.getDirectoryImplementation(),
				session.getLocale());
		}
		return null;
	}

	/**
	 * Determines if a user is in a role
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the service: 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/directory/directory/isUserInRole?userId=<userId>&roleName=<roleName>
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/directory/directory/isUserInRole?userId=admin&roleName=Tech Admin
	 * }
	 * </pre>
	 * @param userId of the desired User
	 * @param roleName of the desired Role
	 * @return boolean true or false.
	 */
	@GET
	@Path("/isUserInRole")
	@Documentation("Determine if the specified user in the specified role.")
	public boolean getisUserinRole(
		@QueryParam("userId") String userId,
		@QueryParam("roleName") String roleName)
	{

		Session session = sessionContext.getSession();
		DirectoryHandler dirHandler = session.getDirectory();
		return dirHandler
			.isUserInRole(UserReference.forUser(userId), Role.forSpecificRole(roleName));
	}

	/**
	 * Get all specific roles for a particular user, including all included roles
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the service: 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/directory/directory/isUserInRole?userId=<userId>
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/directory/directory/isUserInRole?userId=admin
	 * }
	 * </pre>
	 * @param userId of the desired User
	 * @return list of {@link RoleDTO} with the name and label.
	 */
	@GET
	@Path("/specficRolesForUser/{userId}")
	@Documentation("Get all specific roles for a particular user, including all included roles.")
	public Collection<RoleDTO> getSpecficRolesForUser(@PathParam("userId") String userId)
	{

		Session session = sessionContext.getSession();
		DirectoryHandler dirHandler = session.getDirectory();
		if (dirHandler.getDirectoryImplementation() instanceof DirectoryDefault)
		{
			DirectoryDefault directorydefault = (DirectoryDefault) dirHandler
				.getDirectoryImplementation();
			List<Role> roles = directorydefault.getRolesForUser(UserReference.forUser(userId));
			return createRoleDTOs(roles, session.getLocale());
		}
		return null;
	}

	/**
	 * Get the users in the given role.
	 * @param role the given role.
	 * @return list of {@link UserDTO} with the userId and label.
	 */
	private Collection<UserDTO> doGetUsersInRole(Role role)
	{
		Session session = sessionContext.getSession();
		DirectoryHandler dirHandler = session.getDirectory();
		List<UserReference> userReferences = dirHandler.getUsersInRole(role);
		return createUserDTOs(
			userReferences,
			dirHandler.getDirectoryImplementation(),
			session.getLocale());
	}

	/**
	 * Create the {@link UserDTO} objects.
	 * @param userReferences userReferences of the user dtos to be created.
	 * @param directory the directory handler.
	 * @param locale locale.
	 * @return list of {@link UserDTO} with the userId and label.
	 */
	private static Collection<UserDTO> createUserDTOs(
		Collection<UserReference> userReferences,
		Directory directory,
		Locale locale)
	{
		Collection<UserDTO> dtos = new ArrayList<>();
		for (UserReference userReference : userReferences)
		{
			UserDTO dto = new UserDTO();
			dto.setUserId(userReference.getUserId());
			dto.setLabel(directory.displayUser(userReference, locale));
			dtos.add(dto);
		}
		return dtos;
	}

	/**
	 * Create the {@link RoleDTO} objects.
	 * @param roles roles of the role dtos to be created.
	 * @param directory the directory handler.
	 * @param locale locale.
	 * @return list of {@link RoleDTO} with the roleName and label.
	 */
	private static Collection<RoleDTO> createRoleDTOs(Collection<Role> roles, Locale locale)
	{
		Collection<RoleDTO> dtos = new ArrayList<>();
		for (Role role : roles)
		{
			RoleDTO dto = new RoleDTO();
			dto.setName(role.getRoleName());
			dto.setLabel(role.getLabel());
			dtos.add(dto);
		}
		return dtos;
	}

}