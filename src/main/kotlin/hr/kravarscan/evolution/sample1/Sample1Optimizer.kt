package hr.kravarscan.evolution.sample1

import hr.kravarscan.evolution.format
import io.jenetics.DoubleChromosome
import io.jenetics.DoubleGene
import io.jenetics.Genotype
import io.jenetics.Optimize
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.util.Factory

class Sample1Optimizer {
    private val gtf: Factory<Genotype<DoubleGene?>?> =
        Genotype.of(
            // 2 memory inits
            // 3 * 3 action select neurons
            // 1 * 3 answer neurons
            // 2 * 3 state update neurons
            DoubleChromosome.of(-1.0, 1.0, 2 + 9 + 3 + 6),
        )

    private val engine: Engine<DoubleGene?, Double> = Engine
        .builder(::eval, gtf)
        .optimize(Optimize.MINIMUM)
        .build()

    private val trainingSamples = (-40..40).map { SimonSays(it / 40.0) }

    fun next() = engine.stream().limit(1).collect(EvolutionResult.toBestGenotype<DoubleGene?, Double>())

    fun nextFit() = eval(next())

    fun eval(gt: Genotype<DoubleGene?>): Double {
        val gene = gt[0]
        val memInit = arrayOf(gene[0]!!.doubleValue(), gene[1]!!.doubleValue())
        val selectionNns = arrayOf(
            arrayOf(gene[2]!!.doubleValue(), gene[3]!!.doubleValue(), gene[4]!!.doubleValue()),
            arrayOf(gene[5]!!.doubleValue(), gene[6]!!.doubleValue(), gene[7]!!.doubleValue()),
            arrayOf(gene[8]!!.doubleValue(), gene[9]!!.doubleValue(), gene[10]!!.doubleValue())
        )
        val answerNn = arrayOf(gene[11]!!.doubleValue(), gene[12]!!.doubleValue(), gene[13]!!.doubleValue())
        val statUpNns = arrayOf(
            arrayOf(gene[14]!!.doubleValue(), gene[15]!!.doubleValue(), gene[16]!!.doubleValue()),
            arrayOf(gene[17]!!.doubleValue(), gene[18]!!.doubleValue(), gene[19]!!.doubleValue())
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

        return error
    }

    fun dump(gt: Genotype<DoubleGene?>): String {
        val gene = gt[0]
        val memInit = arrayOf(gene[0]!!.doubleValue(), gene[1]!!.doubleValue())
        val selectionNns = arrayOf(
            arrayOf(gene[2]!!.doubleValue(), gene[3]!!.doubleValue(), gene[4]!!.doubleValue()),
            arrayOf(gene[5]!!.doubleValue(), gene[6]!!.doubleValue(), gene[7]!!.doubleValue()),
            arrayOf(gene[8]!!.doubleValue(), gene[9]!!.doubleValue(), gene[10]!!.doubleValue())
        )
        val answerNn = arrayOf(gene[11]!!.doubleValue(), gene[12]!!.doubleValue(), gene[13]!!.doubleValue())
        val statUpNns = arrayOf(
            arrayOf(gene[14]!!.doubleValue(), gene[15]!!.doubleValue(), gene[16]!!.doubleValue()),
            arrayOf(gene[17]!!.doubleValue(), gene[18]!!.doubleValue(), gene[19]!!.doubleValue())
        )

        return "Mem: ${memInit[0].format(2)}, ${memInit[1].format(2)}\n" +
                "Act Get: ${selectionNns[0][0].format(2)}, ${selectionNns[0][1].format(2)}, ${selectionNns[0][2].format(2)}\n" +
                "Act Set: ${selectionNns[1][0].format(2)}, ${selectionNns[1][1].format(2)}, ${selectionNns[1][2].format(2)}\n" +
                "Act Out: ${selectionNns[2][0].format(2)}, ${selectionNns[2][1].format(2)}, ${selectionNns[2][2].format(2)}\n" +
                "Answer: ${answerNn[0].format(2)}, ${answerNn[1].format(2)}, ${answerNn[2].format(2)}\n" +
                "State Get: ${statUpNns[0][0].format(2)}, ${statUpNns[0][1].format(2)}, ${statUpNns[0][2].format(2)}\n" +
                "State Set: ${statUpNns[1][0].format(2)}, ${statUpNns[1][1].format(2)}, ${statUpNns[1][2].format(2)}"
    }
}