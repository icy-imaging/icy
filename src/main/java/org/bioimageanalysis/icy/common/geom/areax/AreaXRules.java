/*
 * Copyright (c) 2010-2024. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * This software is released as part of the Pumpernickel project.
 * All com.pump resources in the Pumpernickel project are distributed under the
 * MIT License:
 * https://github.com/mickleness/pumpernickel/raw/master/License.txt
 * More information about the Pumpernickel project is available here:
 * https://mickleness.github.io/pumpernickel/
 */
package org.bioimageanalysis.icy.common.geom.areax;

import java.awt.Shape;

/**
 * This executes several geometric operations at once on a
 * <code>AreaXBody</code> object.
 * <p>
 * This class sequentially interprets each operation, but the reason this class
 * exists is for subclasses to come along and cleverly optimize how these calls
 * are made.
 * <p>
 * For example: If it can be determined that a new shape lies inside an existing
 * shape, then that addition doesn't have to performed. Also adding is
 * commutative, so if a filter for redundant additions is in place <i>and</i>
 * the add operations are sorted from bigger-to-smaller shapes: 100 add
 * operations could -- in specific instances -- be simplified to just 1 or 2
 * operations.
 * <p>
 * All AreaXRules should return the same final result when asked to execute the
 * same steps.
 * <p>
 * Also an instance of this object may be applied to multiple <code>AreaX</code>
 * objects, so it is important that this object not use its own class-level
 * variables. Several different threads may call <code>execute(...)</code> for
 * different operations at the same time.
 */
public class AreaXRules
{
    /**
     * @param body
     * @param ops
     *        an array of operations to perform. Null elements in this array
     *        are skipped.
     */
    public AreaXBody execute(AreaXBody body, AreaXOperation[] ops)
    {
        return execute(body, 0, ops.length - 1, ops);
    }

    /**
     * @param body
     * @param ops
     *        an array of operations to perform. Null elements in this array
     *        are skipped.
     */
    public AreaXBody execute(AreaXBody body, int startIndex, int endIndex, AreaXOperation[] ops)
    {
        for (int a = startIndex; a <= endIndex; a++)
        {
            if (ops[a] != null)
            {
                Shape operand = ops[a].getOperand();
                AreaXBody operandBody;
                if (operand instanceof AreaX)
                {
                    operandBody = ((AreaX) operand).getBody();
                }
                else
                {
                    operandBody = new AreaX(operand).getBody();
                }
                if (ops[a].getOperator() == AreaXOperation.ADD)
                {
                    body = body.add(operandBody);
                }
                else if (ops[a].getOperator() == AreaXOperation.SUBTRACT)
                {
                    body = body.subtract(operandBody);
                }
                else if (ops[a].getOperator() == AreaXOperation.INTERSECT)
                {
                    body = body.intersect(operandBody);
                }
                else if (ops[a].getOperator() == AreaXOperation.XOR)
                {
                    body = body.xor(operandBody);
                }
            }
        }
        return body;
    }
}