/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ssehub.kernel_haven.non_boolean.replacer;

import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * A boolean {@link Result}. This is an operator (&&, ||, !) or a {@link LiteralBoolResult}.
 *
 * @author Adam
 */
abstract class BoolResult extends Result {

    @Override
    public Result cmpLt(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator < or > on boolean");
    }

    @Override
    public Result cmpLe(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator <= or >= on boolean");
    }
    
    @Override
    public Result cmpEq(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator == or != on boolean");
    }
    
    @Override
    public Result add(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator + on boolean");
    }
    @Override
    public Result sub(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator - on boolean");
    }
    @Override
    public Result mul(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator * on boolean");
    }
    @Override
    public Result div(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator / on boolean");
    }
    @Override
    public Result mod(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator % on boolean");
    }
    
    @Override
    public Result subUnary() throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator unary subtraction on boolean");
    }

    @Override
    public Result binAnd(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator & on boolean");
    }

    @Override
    public Result binOr(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator | on boolean");
    }

    @Override
    public Result binXor(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator ^ on boolean");
    }

    @Override
    public Result binInv() throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator ~ on boolean");
    }

}
