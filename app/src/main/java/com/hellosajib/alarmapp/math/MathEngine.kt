package com.hellosajib.alarmapp.math

data class MathProblem(
    val operand1: Int,
    val operand2: Int,
    val operation: Char,
    val solution: Int
) {
    val expression: String get() = "$operand1 $operation $operand2"
}

object MathEngine {
    fun generateProblem(excludeProblem: MathProblem? = null): MathProblem {
        var problem: MathProblem
        var attempts = 0
        do {
            val isAddition = kotlin.random.Random.nextBoolean()
            if (isAddition) {
                // Two digit addition: 12..89 + 11..89
                val a = kotlin.random.Random.nextInt(15, 85)
                val b = kotlin.random.Random.nextInt(12, 75)
                problem = MathProblem(operand1 = a, operand2 = b, operation = '+', solution = a + b)
            } else {
                // Two digit subtraction with positive answer: a in 30..99, b in 12..(a-10)
                val a = kotlin.random.Random.nextInt(32, 98)
                val b = kotlin.random.Random.nextInt(11, a - 8)
                problem = MathProblem(operand1 = a, operand2 = b, operation = '-', solution = a - b)
            }
            attempts++
        } while (excludeProblem != null && excludeProblem.expression == problem.expression && attempts < 5)

        return problem
    }

    fun isAnswerCorrect(problem: MathProblem, answerStr: String): Boolean {
        val parsed = answerStr.trim().toIntOrNull() ?: return false
        return parsed == problem.solution
    }
}
