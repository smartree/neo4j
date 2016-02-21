/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.coreedge.raft.log;

import java.io.File;
import java.io.IOException;

import org.neo4j.io.file.Files;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.kernel.impl.transaction.log.LogVersionRepository;
import org.neo4j.kernel.impl.transaction.log.PhysicalLogFiles;

public class FilenameBasedLogVersionRepository implements LogVersionRepository
{
    private long version = 0;

    public FilenameBasedLogVersionRepository( PhysicalLogFiles logFiles)
    {
        version = Math.max(logFiles.getHighestLogVersion(), 0);
    }


    @Override
    public long getCurrentLogVersion()
    {
        return version;
    }

    @Override
    public long incrementAndGetVersion() throws IOException
    {
        return ++version;
    }
}