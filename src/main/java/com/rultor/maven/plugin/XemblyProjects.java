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

import com.rultor.snapshot.XemblyLine;
import com.rultor.tools.Exceptions;
import com.rultor.tools.Time;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.xembly.Directives;

/**
 * Reports projects.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.2
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
final class XemblyProjects implements ExecutionListener {

    /**
     * Start times of given projects.
     */
    private final transient ConcurrentMap<String, Long> times =
        new ConcurrentHashMap<String, Long>(0);

    /**
     * Target execution origin.
     */
    private final transient ExecutionListener origin;

    /**
     * Constructor.
     *
     * @param lstnr Listener to call.
     */
    XemblyProjects(final ExecutionListener lstnr) {
        this.origin = lstnr;
    }

    @Override
    public void projectDiscoveryStarted(final ExecutionEvent event) {
        this.origin.projectDiscoveryStarted(event);
    }

    @Override
    public void sessionStarted(final ExecutionEvent event) {
        this.origin.sessionStarted(event);
    }

    @Override
    public void sessionEnded(final ExecutionEvent event) {
        this.origin.sessionEnded(event);
    }

    @Override
    public void projectSkipped(final ExecutionEvent event) {
        final String name = XemblyProjects.identifier(event);
        new XemblyLine(
            new Directives()
                .xpath("/snapshot").strict(1)
                .addIf("steps").add("step")
                .attr("id", name)
                .add("summary")
                .set(String.format("project `%s` skipped", name)).up()
                .add("start").set(new Time().toString()).up()
                .add("finish").set(new Time().toString()).up()
                .add("level").set(Level.INFO.toString())
        ).log();
        this.origin.mojoStarted(event);
    }

    @Override
    public void projectStarted(final ExecutionEvent event) {
        final String name = XemblyProjects.identifier(event);
        final Time start = new Time();
        this.times.put(name, start.millis());
        new XemblyLine(
            new Directives()
                .xpath("/snapshot").strict(1)
                .addIf("steps").add("step")
                .attr("id", name)
                .add("summary")
                .set(String.format("`%s` running...", name))
                .up()
                .add("start").set(start.toString()).up()
        ).log();
        this.origin.projectStarted(event);
    }

    @Override
    public void projectSucceeded(final ExecutionEvent event) {
        final String name = XemblyProjects.identifier(event);
        if (this.times.containsKey(name)) {
            final long start = this.times.get(name);
            final Time end = new Time();
            new XemblyLine(
                new Directives()
                    .xpath("/snapshot/steps").strict(1)
                    .xpath(String.format("step[@id='%s']/summary", name))
                    .set(String.format("`%s`", name)).up()
                    .add("finish").set(end.toString()).up()
                    .add("level").set(Level.INFO.toString()).up()
                    .add("duration").set(Long.toString(end.millis() - start))
            ).log();
        }
        this.origin.projectSucceeded(event);
    }

    @Override
    public void projectFailed(final ExecutionEvent event) {
        final String name = XemblyProjects.identifier(event);
        new XemblyLine(
            new Directives()
                .xpath(String.format("/snapshot/steps/step[@id=%s]", name))
                .add("exception").add("class")
                .set(event.getException().getClass().getCanonicalName()).up()
                .add("stacktrace")
                .set(Exceptions.stacktrace(event.getException())).up()
                .add("level").set(Level.SEVERE.toString()).up()
                .add("cause").set(Exceptions.message(event.getException()))
        ).log();
        this.origin.projectFailed(event);
    }

    @Override
    public void mojoSkipped(final ExecutionEvent event) {
        this.origin.mojoSkipped(event);
    }

    @Override
    public void mojoStarted(final ExecutionEvent event) {
        this.origin.mojoStarted(event);
    }

    @Override
    public void mojoSucceeded(final ExecutionEvent event) {
        this.origin.mojoSucceeded(event);
    }

    @Override
    public void mojoFailed(final ExecutionEvent event) {
        this.origin.mojoFailed(event);
    }

    @Override
    public void forkStarted(final ExecutionEvent event) {
        this.origin.forkStarted(event);
    }

    @Override
    public void forkSucceeded(final ExecutionEvent event) {
        this.origin.forkSucceeded(event);
    }

    @Override
    public void forkFailed(final ExecutionEvent event) {
        this.origin.forkFailed(event);
    }

    @Override
    public void forkedProjectStarted(final ExecutionEvent event) {
        this.origin.forkedProjectStarted(event);
    }

    @Override
    public void forkedProjectSucceeded(final ExecutionEvent event) {
        this.origin.forkedProjectSucceeded(event);
    }

    @Override
    public void forkedProjectFailed(final ExecutionEvent event) {
        this.origin.forkedProjectFailed(event);
    }

    /**
     * Identifier of given event.
     *
     * @param event Event to identify.
     * @return Identifier.
     */
    private static String identifier(final ExecutionEvent event) {
        return String.format(
            "%s:%s",
            event.getProject().getGroupId(),
            event.getProject().getArtifactId()
        );
    }
}
