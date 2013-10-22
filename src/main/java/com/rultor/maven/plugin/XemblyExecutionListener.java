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
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.xembly.Directives;

/**
 * Listener that submits Xemblies.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
final class XemblyExecutionListener implements ExecutionListener {

    /**
     * Start times of given goal inside artifacts.
     */
    private final transient ConcurrentMap<String, Long> times =
        new ConcurrentHashMap<String, Long>(0);

    /**
     * Target execution listener.
     */
    private final transient ExecutionListener listener;

    /**
     * Constructor.
     *
     * @param lstnr Listener to call.
     */
    public XemblyExecutionListener(final ExecutionListener lstnr) {
        this.listener = lstnr;
    }

    @Override
    public void projectDiscoveryStarted(final ExecutionEvent event) {
        this.listener.projectDiscoveryStarted(event);
    }

    @Override
    public void sessionStarted(final ExecutionEvent event) {
        this.listener.sessionStarted(event);
    }

    @Override
    public void sessionEnded(final ExecutionEvent event) {
        this.listener.sessionEnded(event);
    }

    @Override
    public void projectSkipped(final ExecutionEvent event) {
        this.listener.projectSkipped(event);
    }

    @Override
    public void projectStarted(final ExecutionEvent event) {
        this.listener.projectStarted(event);
    }

    @Override
    public void projectSucceeded(final ExecutionEvent event) {
        this.listener.projectSucceeded(event);
    }

    @Override
    public void projectFailed(final ExecutionEvent event) {
        this.listener.projectFailed(event);
    }

    @Override
    public void mojoSkipped(final ExecutionEvent event) {
        this.listener.mojoSkipped(event);
    }

    @Override
    public void mojoStarted(final ExecutionEvent event) {
        final Time start = new Time();
        this.times.put(this.identifier(event), start.millis());
        new XemblyLine(
            new Directives()
                .xpath("/snapshot")
                .strict(1)
                .addIf("steps")
                .add("step")
                .attr("id", this.identifier(event))
                .add("summary")
                .set(
                    String.format(
                        "mojo `%s` running",
                        this.identifier(event)
                    )
                )
                .up()
                .add("start").set(start.toString()).up()
        ).log();
        this.listener.mojoStarted(event);
    }

    @Override
    public void mojoSucceeded(final ExecutionEvent event) {
        if (this.times.containsKey(this.identifier(event))) {
            final long start = this.times.get(this.identifier(event));
            final Time end = new Time();
            new XemblyLine(
                new Directives()
                    .xpath("/snapshot/steps")
                    .strict(1)
                    .xpath(
                        String.format(
                            "step[@id='%s']/summary",
                            this.identifier(event)
                        )
                    )
                    .set(
                        String.format(
                            "target `%s` finished", this.identifier(event)
                        )
                    )
                    .up()
                    .add("finish").set(end.toString()).up()
                    .add("duration")
                    .set(Long.toString(end.millis() - start))
            ).log();
        }
        this.listener.mojoSucceeded(event);
    }

    /**
     * Identifier of given event.
     *
     * @param event Event to identify.
     * @return Identifier.
     */
    private String identifier(final ExecutionEvent event) {
        return String.format(
            "%s:%s:%s",
            event.getMojoExecution().getGroupId(),
            event.getMojoExecution().getArtifactId(),
            event.getMojoExecution().getGoal()
        );
    }

    @Override
    public void mojoFailed(final ExecutionEvent event) {
        new XemblyLine(
            new Directives().xpath(
                String.format(
                    "/snapshot/steps/step[@id=%s]",
                    this.identifier(event)
                )
            )
                .add("exception")
                .add("class")
                .set(event.getException().getClass().getCanonicalName())
                .up()
                .add("stacktrace")
                .set(Exceptions.stacktrace(event.getException())).up()
                .add("cause").set(Exceptions.message(event.getException()))
        ).log();
        this.listener.mojoFailed(event);
    }

    @Override
    public void forkStarted(final ExecutionEvent event) {
        this.listener.forkStarted(event);
    }

    @Override
    public void forkSucceeded(final ExecutionEvent event) {
        this.listener.forkSucceeded(event);
    }

    @Override
    public void forkFailed(final ExecutionEvent event) {
        this.listener.forkFailed(event);
    }

    @Override
    public void forkedProjectStarted(final ExecutionEvent event) {
        this.listener.forkedProjectStarted(event);
    }

    @Override
    public void forkedProjectSucceeded(final ExecutionEvent event) {
        this.listener.forkedProjectSucceeded(event);
    }

    @Override
    public void forkedProjectFailed(final ExecutionEvent event) {
        this.listener.forkedProjectFailed(event);
    }
}
