/**
 * Copyright (c) 2012-2015, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
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
package com.jcabi.jdbc;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Outcome that returns a list.
 *
 * <p>Use it when you need a full collection:
 *
 * <pre> Collection&lgt;User&gt; users = new JdbcSession(source)
 *   .sql("SELECT * FROM user")
 *   .select(
 *     new ListOutcome&lt;User&gt;(
 *       new ListOutcome.Mapping&lt;User&gt;() {
 *         &#64;Override
 *         public User map(final ResultSet rset) throws SQLException {
 *           return new User.Simple(rset.getLong(1), rset.getString(2));
 *         }
 *       }
 *     )
 *   );</pre>
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 0.13
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "mapping")
public final class ListOutcome<T> implements Outcome<List<T>> {

    /**
     * Mapping.
     */
    private final transient ListOutcome.Mapping<T> mapping;

    /**
     * Public ctor.
     * @param mpg Mapping
     */
    public ListOutcome(@NotNull(message = "mapping can't be NULL")
        final ListOutcome.Mapping<T> mpg) {
        this.mapping = mpg;
    }

    @Override
    @Loggable(Loggable.DEBUG)
    public List<T> handle(final ResultSet rset, final Statement stmt)
        throws SQLException {
        final List<T> result = new LinkedList<T>();
        while (rset.next()) {
            result.add(this.mapping.map(rset));
        }
        return result;
    }

    /**
     * Mapping.
     */
    @Immutable
    public interface Mapping<T> {
        /**
         * Map.
         * @param rset Result set
         * @return Object
         * @throws SQLException If fails
         */
        T map(ResultSet rset) throws SQLException;
    }

}
