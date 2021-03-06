/*
 * Build system for LensKit, and open-source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 *
 * - Neither the name of the University of Minnesota nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.grouplens.lenskit.build

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class GitExtension {
    private Repository repo

    /**
     * Invoke a block with an open Git repository.  The block will receive the
     * Repository object as its first parameter, and optionally a Git object as
     * its second.  This method takes care of closing the repository.
     */
    public <V> V withRepo(Closure<V> block) {
        def bld = new FileRepositoryBuilder()
        if (repo == null) {
            repo = bld.readEnvironment().findGitDir().build()
            try {
                invokeWithRepo(block)
            } finally {
                try {
                    repo.close()
                } finally {
                    repo = null
                }
            }
        } else {
            invokeWithRepo(block)
        }
    }

    private <V> V invokeWithRepo(Closure<V> block) {
        if (block.maximumNumberOfParameters > 1) {
            block.call(repo, new Git(repo))
        } else {
            block.call(repo)
        }
    }

    ObjectId getHeadRevision() {
        withRepo { Repository repo -> repo.resolve(Constants.HEAD) }
    }

    RevCommit getHeadCommit() {
        withRepo { Repository repo ->
            def walk = new RevWalk(repo)
            walk.parseCommit(headRevision)
        }
    }
}
