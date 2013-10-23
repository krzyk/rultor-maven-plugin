/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.maven.plugin;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Steps Mojo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@ToString
@Mojo(
    name = "steps", defaultPhase = LifecyclePhase.INITIALIZE,
    requiresProject = true, threadSafe = true,
    instantiationStrategy = InstantiationStrategy.SINGLETON
)
@EqualsAndHashCode(callSuper = false)
@Loggable(Loggable.DEBUG)
public final class StepsMojo extends AbstractMojo {

    /**
     * Maven session, to be injected by Maven itself.
     */
    @Component
    private transient MavenSession session;

    /**
     * Maven project, to be injected by Maven itself.
     */
    @Component
    private transient MavenProject project;

    /**
     * Skip execution.
     * @since 0.3
     */
    @Parameter(property = "rultor.skip", defaultValue = "true")
    private transient boolean skip;

    /**
     * Listener already injected.
     * @since 0.3
     */
    private transient boolean injected;

    @Override
    public void execute() {
        if (this.injected) {
            Logger.info(this, "Xembly listener already injected");
        } else if (this.skip) {
            Logger.info(this, "Execution skipped, use -Drultor.skip=false");
        } else {
            this.inject();
            Logger.info(this, "Xembly execution listener injected");
        }
    }

    /**
     * Set session.
     * @param ssn Session to set.
     */
    public void setSession(final MavenSession ssn) {
        this.session = ssn;
    }

    /**
     * Set project.
     * @param prj Project to inject
     */
    public void setProject(final MavenProject prj) {
        this.project = prj;
    }

    /**
     * Inject listener.
     */
    private void inject() {
        final MavenExecutionRequest request = this.session.getRequest();
        ExecutionListener listener = request.getExecutionListener();
        if (this.project.getModules().isEmpty()) {
            listener = new XemblyMojos(listener);
        } else {
            listener = new XemblyProjects(listener);
        }
        request.setExecutionListener(listener);
        this.injected = true;
    }

}
