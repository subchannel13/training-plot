package hr.kravarscan.evolution

import io.jenetics.BitChromosome
import io.jenetics.BitGene
import io.jenetics.Genotype
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.util.Factory
import kotlin.streams.asSequence

class JExample {
    // 1.) Define the genotype (factory) suitable
    //     for the problem.
    private val gtf: Factory<Genotype<BitGene?>?> =
        Genotype.of<BitGene?>(BitChromosome.of(10, 0.5))


    // 3.) Create the execution environment.
    private val engine: Engine<BitGene?, Int> = Engine
        .builder(::eval, gtf)
        .build()


    // 4.) Start the execution (evolution) and
    //     collect the result.
    private val resultStream = engine.stream()
        /*.limit(100)
        .collect(EvolutionResult.toBestGenotype<BitGene?, Int>())*/

    // 2.) Definition of the fitness function.
    private fun eval(gt: Genotype<BitGene?>): Int {
        return gt.chromosome()
            .`as`(BitChromosome::class.java)!!
            .bitCount()
    }

    fun next() = engine.stream().limit(1).collect(EvolutionResult.toBestGenotype<BitGene?, Int>())

    fun nextFit() = eval(next())

    suspend fun exampleMain() {
        // 1.) Define the genotype (factory) suitable
        //     for the problem.
        val gtf: Factory<Genotype<BitGene?>?> =
            Genotype.of<BitGene?>(BitChromosome.of(10, 0.5))


        // 3.) Create the execution environment.
        val engine: Engine<BitGene?, Int> = Engine
            .builder(::eval, gtf)
            .build()


        // 4.) Start the execution (evolution) and
        //     collect the result.
        val result = engine.stream()
            .limit(100)
            .collect(EvolutionResult.toBestGenotype<BitGene?, Int>())

        println("Hello World: $result\n")
    }
}