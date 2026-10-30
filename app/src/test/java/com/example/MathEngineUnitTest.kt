package com.example

import com.example.math.MathEngine
import com.example.math.MathProblem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MathEngineUnitTest {

    @Test
    fun testMathProblemGenerationAndAnswerValidation() {
        for (i in 0 until 50) {
            val problem = MathEngine.generateProblem()
            assertTrue(problem.operand1 > 0)
            assertTrue(problem.operand2 > 0)

            if (problem.operation == '+') {
                assertEquals(problem.operand1 + problem.operand2, problem.solution)
            } else {
                assertEquals(problem.operand1 - problem.operand2, problem.solution)
                assertTrue(problem.solution > 0)
            }

            assertTrue(MathEngine.isAnswerCorrect(problem, problem.solution.toString()))
            assertFalse(MathEngine.isAnswerCorrect(problem, (problem.solution + 1).toString()))
            assertFalse(MathEngine.isAnswerCorrect(problem, "abc"))
        }
    }
}
