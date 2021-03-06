/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.commandline.admin.security;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import org.neo4j.commandline.admin.AdminTool;
import org.neo4j.commandline.admin.BlockerLocator;
import org.neo4j.commandline.admin.CommandLocator;
import org.neo4j.commandline.admin.OutsideWorld;
import org.neo4j.graphdb.mockfs.EphemeralFileSystemAbstraction;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.logging.NullLogProvider;
import org.neo4j.server.security.auth.FileUserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SetDefaultAdminCommandIT
{
    private FileSystemAbstraction fileSystem = new EphemeralFileSystemAbstraction();
    private File confDir;
    private File homeDir;
    private OutsideWorld out;
    private AdminTool tool;

    private static final String SET_ADMIN = "set-default-admin";

    @Before
    public void setup()
    {
        File graphDir = new File( "graph-db" );
        confDir = new File( graphDir, "conf" );
        homeDir = new File( graphDir, "home" );
        out = mock( OutsideWorld.class );
        resetOutsideWorldMock();
        tool = new AdminTool( CommandLocator.fromServiceLocator(), BlockerLocator.fromServiceLocator(), out, true );
    }

    @Test
    public void shouldSetDefaultAdmin() throws Throwable
    {
        tool.execute( homeDir.toPath(), confDir.toPath(), SET_ADMIN, "jane" );
        assertAdminIniFile( "jane" );

        verify( out ).stdOutLine( "Set default admin to 'jane'." );
    }

    @Test
    public void shouldOverwrite() throws Throwable
    {
        tool.execute( homeDir.toPath(), confDir.toPath(), SET_ADMIN, "jane" );
        assertAdminIniFile( "jane" );
        tool.execute( homeDir.toPath(), confDir.toPath(), SET_ADMIN, "janette" );
        assertAdminIniFile( "janette" );

        verify( out ).stdOutLine( "Set default admin to 'jane'." );
        verify( out ).stdOutLine( "Set default admin to 'janette'." );
    }

    @Test
    public void shouldGetUsageOnWrongArguments() throws Throwable
    {
        tool.execute( homeDir.toPath(), confDir.toPath(), SET_ADMIN );
        tool.execute( homeDir.toPath(), confDir.toPath(), SET_ADMIN, "foo", "bar" );
        assertNoAuthIniFile();

        verify( out, times( 2 ) ).stdErrLine( "neo4j-admin set-default-admin <username>" );
        verify( out, times( 0 ) ).stdOutLine( anyString() );
    }

    private void assertAdminIniFile( String username ) throws Throwable
    {
        File adminIniFile = getAuthFile( SetDefaultAdminCommand.ADMIN_INI );
        assertTrue( fileSystem.fileExists( adminIniFile ) );
        FileUserRepository userRepository = new FileUserRepository( fileSystem, adminIniFile,
                NullLogProvider.getInstance() );
        userRepository.start();
        assertThat( userRepository.getAllUsernames(), containsInAnyOrder( username ) );
    }

    private void assertNoAuthIniFile()
    {
        assertFalse( fileSystem.fileExists( getAuthFile( SetDefaultAdminCommand.ADMIN_INI ) ) );
    }

    private File getAuthFile( String name )
    {
        return new File( new File( new File( homeDir, "data" ), "dbms" ), name );
    }

    private void resetOutsideWorldMock()
    {
        reset(out);
        when( out.fileSystem() ).thenReturn( fileSystem );
    }
}
