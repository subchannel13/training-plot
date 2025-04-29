package hr.kravarscan.evolution.sample1

import hr.kravarscan.evolution.format
import io.jenetics.BitChromosome
import io.jenetics.BitGene
import io.jenetics.Genotype
import io.jenetics.Optimize
import io.jenetics.engine.Codec
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.util.Factory

class Sample1OptimizerBinary {
    private val precision = 4

    private val genotype: Factory<Genotype<BitGene?>?> =
        Genotype.of(
            // 2 memory inits
            // 3 * 3 action select neurons
            // 1 * 3 answer neurons
            // 2 * 3 state update neurons
            //
            BitChromosome.of(precision * (2 + 9 + 3 + 6)),
        )

    private val codec = Codec.of<DoubleArray, BitGene>(genotype) {
        val chromosome = it[0] as BitChromosome
        val len = chromosome.length() / precision
        val result = DoubleArray(len)
        val data = chromosome.toByteArray()
        var iBit = 0
        var iByte = 0
        val halfI = 1 shl precision - 1
        val halfF = halfI.toDouble()

        for(i in 0 ..< len) {
            var x = 0
            for(j in 0 ..< precision) {
                if (data[iByte].toInt() and (1 shl iBit) != 0)
                    x += 1 shl j

                iBit++
                if (iBit >= 8) {
                    iBit = 0
                    iByte++
                }
            }

            result[i] = (x - halfI) / halfF
        }
        result
    }

    private val engine: Engine<BitGene?, Double> = Engine
        .builder(::eval, genotype)
        .optimize(Optimize.MINIMUM)
        .build()

    private val trainingSamples = (-40..40).map { SimonSays(it / 40.0) }

    fun next() = engine.stream().limit(1).collect(EvolutionResult.toBestGenotype<BitGene?, Double>())

    fun nextFit() = eval(next())

    fun eval(gt: Genotype<BitGene?>): Double {
        val gene = codec.decode(gt)
        val memInit = arrayOf(gene[0], gene[1])
        val selectionNns = arrayOf(
            arrayOf(gene[2], gene[3], gene[4]),
            arrayOf(gene[5], gene[6], gene[7]),
            arrayOf(gene[8], gene[9], gene[10])
        )
        val answerNn = arrayOf(gene[11], gene[12], gene[13])
        val statUpNns = arrayOf(
            arrayOf(gene[14], gene[15], gene[16]),
            arrayOf(gene[17], gene[18], gene[19])
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

    fun dump(gt: Genotype<BitGene?>): String {
        val gene = codec.decode(gt)
        val memInit = arrayOf(gene[0], gene[1])
        val selectionNns = arrayOf(
            arrayOf(gene[2], gene[3], gene[4]),
            arrayOf(gene[5], gene[6], gene[7]),
            arrayOf(gene[8], gene[9], gene[10])
        )
        val answerNn = arrayOf(gene[11], gene[12], gene[13])
        val statUpNns = arrayOf(
            arrayOf(gene[14], gene[15], gene[16]),
            arrayOf(gene[17], gene[18], gene[19])
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