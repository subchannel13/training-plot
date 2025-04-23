package hr.kravarscan.evolution.sample1

import io.jenetics.BitGene
import io.jenetics.DoubleChromosome
import io.jenetics.DoubleGene
import io.jenetics.Genotype
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.util.Factory

class Sample1Optimizer {
    private val gtf: Factory<Genotype<DoubleGene?>?> =
        Genotype.of(
            // Memory
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),

            // Select action 1
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),
            // Select action 2
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),
            // Select action 3
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),

            // Answer number
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),

            // Action 1 state update
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),
            // Action 2 state update
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),
            DoubleChromosome.of(-1.0, 1.0),
        )

    private val engine: Engine<DoubleGene?, Double> = Engine
        .builder(::eval, gtf)
        .build()

    private val trainingSamples = (-40..40).map { SimonSays(it / 40.0) }

    private val resultStream = engine.stream()

    fun next() = engine.stream().limit(1).collect(EvolutionResult.toBestGenotype<DoubleGene?, Double>())

    fun nextFit() = eval(next())

    fun eval(gt: Genotype<DoubleGene?>): Double {
        val memInit = arrayOf(gt[0].gene()!!.doubleValue(), gt[1].gene()!!.doubleValue())
        val selectionNns = arrayOf(
            arrayOf(gt[2].gene()!!.doubleValue(), gt[3].gene()!!.doubleValue(), gt[4].gene()!!.doubleValue()),
            arrayOf(gt[5].gene()!!.doubleValue(), gt[6].gene()!!.doubleValue(), gt[7].gene()!!.doubleValue()),
            arrayOf(gt[8].gene()!!.doubleValue(), gt[9].gene()!!.doubleValue(), gt[10].gene()!!.doubleValue())
        )
        val answerNn = arrayOf(gt[11].gene()!!.doubleValue(), gt[12].gene()!!.doubleValue(), gt[13].gene()!!.doubleValue())
        val statUpNns = arrayOf(
            arrayOf(gt[14].gene()!!.doubleValue(), gt[15].gene()!!.doubleValue(), gt[16].gene()!!.doubleValue()),
            arrayOf(gt[17].gene()!!.doubleValue(), gt[18].gene()!!.doubleValue(), gt[19].gene()!!.doubleValue())
        )

        val mem = memInit.copyOf()

        var error = 0.0
        for (sample in trainingSamples) {
            memInit.copyInto(mem)
            for(iteration in 0 .. 100) {
                var maxActivation = Double.NEGATIVE_INFINITY
                var actionI = -1
                for (i in 0..<3) {
                    val nn = selectionNns[i]
                    val activation = mem[0] * nn[0] + mem[1] * nn[1] + nn[2]
                    if (activation > maxActivation) {
                        maxActivation = activation
                        actionI = i
                    }
                }

                when (actionI) {
                    0 -> {
                        mem[1] = sample.question()
                        val nn = statUpNns[0]
                        mem[0] = mem[0] * nn[0] + mem[1] * nn[1] + nn[2]
                    }
                    1 -> {
                        sample.answer(mem[0] * answerNn[0] + mem[1] * answerNn[1] + answerNn[2])
                        val nn = statUpNns[0]
                        mem[0] = mem[0] * nn[0] + mem[1] * nn[1] + nn[2]
                    }
                    2 -> break
                }
            }

            error += sample.error.coerceAtMost(1.0)
        }

        return trainingSamples.size - error
    }

    fun dump(gt: Genotype<DoubleGene?>): String {
        val memInit = arrayOf(gt[0].gene()!!.doubleValue(), gt[1].gene()!!.doubleValue())
        val selectionNns = arrayOf(
            arrayOf(gt[2].gene()!!.doubleValue(), gt[3].gene()!!.doubleValue(), gt[4].gene()!!.doubleValue()),
            arrayOf(gt[5].gene()!!.doubleValue(), gt[6].gene()!!.doubleValue(), gt[7].gene()!!.doubleValue()),
            arrayOf(gt[8].gene()!!.doubleValue(), gt[9].gene()!!.doubleValue(), gt[10].gene()!!.doubleValue())
        )
        val answerNn = arrayOf(gt[11].gene()!!.doubleValue(), gt[12].gene()!!.doubleValue(), gt[13].gene()!!.doubleValue())
        val statUpNns = arrayOf(
            arrayOf(gt[14].gene()!!.doubleValue(), gt[15].gene()!!.doubleValue(), gt[16].gene()!!.doubleValue()),
            arrayOf(gt[17].gene()!!.doubleValue(), gt[18].gene()!!.doubleValue(), gt[19].gene()!!.doubleValue())
        )

        return "Mem: ${memInit[0].format(2)}, ${memInit[1].format(2)}\n" +
                "Act Get: ${selectionNns[0][0].format(2)}, ${selectionNns[0][1].format(2)}, ${selectionNns[0][2].format(2)}\n" +
                "Act Set: ${selectionNns[1][0].format(2)}, ${selectionNns[1][1].format(2)}, ${selectionNns[1][2].format(2)}\n" +
                "Act Out: ${selectionNns[2][0].format(2)}, ${selectionNns[2][1].format(2)}, ${selectionNns[2][2].format(2)}\n" +
                "Answer: ${answerNn[0].format(2)}, ${answerNn[1].format(2)}, ${answerNn[2].format(2)}\n" +
                "State Get: ${statUpNns[0][0].format(2)}, ${statUpNns[0][1].format(2)}, ${statUpNns[0][2].format(2)}\n" +
                "State Set: ${statUpNns[1][0].format(2)}, ${statUpNns[1][1].format(2)}, ${statUpNns[1][2].format(2)}"
    }

    companion object {
        fun Double.format(digits: Int) = "%.${digits}f".format(this)
    }
}