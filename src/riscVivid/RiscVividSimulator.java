/*******************************************************************************
 * riscVivid - A RISC-V processor simulator.
 * (C)opyright 2013-2016 The riscVivid project, University of Augsburg, Germany
 * https://github.com/unia-sik/riscVivid
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, see <LICENSE>. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package riscVivid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import riscVivid.datatypes.ArchCfg;
import riscVivid.datatypes.BranchPredictionModuleExecuteData;
import riscVivid.datatypes.BranchPredictionModuleFetchData;
import riscVivid.datatypes.BranchPredictionModuleOutputData;
import riscVivid.datatypes.DecodeExecuteData;
import riscVivid.datatypes.DecodeOutputData;
import riscVivid.datatypes.ExecuteBranchPredictionData;
import riscVivid.datatypes.ExecuteFetchData;
import riscVivid.datatypes.ExecuteMemoryData;
import riscVivid.datatypes.ExecuteOutputData;
import riscVivid.datatypes.FetchDecodeData;
import riscVivid.datatypes.FetchOutputData;
import riscVivid.datatypes.ISAType;
import riscVivid.datatypes.Instruction;
import riscVivid.datatypes.MemoryOutputData;
import riscVivid.datatypes.MemoryWritebackData;
import riscVivid.datatypes.SpecialRegisters;
import riscVivid.datatypes.WriteBackData;
import riscVivid.datatypes.WritebackOutputData;
import riscVivid.datatypes.uint32;
import riscVivid.datatypes.uint8;
import riscVivid.exception.DecodeStageException;
import riscVivid.exception.MemoryException;
import riscVivid.exception.PipelineException;
import riscVivid.gui.GUI_CONST;
import riscVivid.memory.DataMemory;
import riscVivid.memory.InstructionMemory;
import riscVivid.memory.MainMemory;
import riscVivid.util.ClockCycleLog;
import riscVivid.util.LoggerConfigurator;
import riscVivid.util.RISCVSyscallHandler;
import riscVivid.util.Statistics;

public class RiscVividSimulator
{

    private static Logger logger = Logger.getLogger("riscVivid");
    private PipelineContainer pipeline;
    private Properties config;
    private Statistics stat;
    private boolean caught_break = false;
    private int clock_cycle;
    private int sim_cycles;
    private boolean finished;

    /**
     * @param args
     */
    public void riscVividCmdl_main()
    {

        while (!finished)
        {
            try
            {
                step();
            }
            catch (PipelineException e)
            {
                e.printStackTrace();
                stopSimulation(true);
            }
        }
    }


    public RiscVividSimulator(String configfile) throws PipelineException
    {
        config = new Properties();

        try
        {
            config.load(new FileInputStream(configfile));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        setDefaultConfigParameters(config);
            // CAUTION: if no config file exists, asm.AsmFileLoader.saveToFile()
            // creates a config file and uses different default values

        ArchCfg.registerArchitectureConfig(config);

        System.out.println("Configuration is: " + config.toString());

        LoggerConfigurator.getInstance().configureLogger(config.getProperty("log4j"), config.getProperty("log_file"));

        logger.info("Configuration is: " + config.toString());
        logger.info("loading:" + config.getProperty("file"));


        sim_cycles = Integer.parseInt(config.getProperty("cycles"));

        pipeline = new PipelineContainer();
        pipeline.setMainMemory(new MainMemory(config.getProperty("file"), stringToUint32(config.getProperty("code_start_addr")).getValue(),
        		(short) stringToUint32(config.getProperty("memory_latency")).getValue()));
        pipeline.setInstructionMemory(new InstructionMemory(pipeline.getMainMemory(), config));
        pipeline.setDataMemory(new DataMemory(pipeline.getMainMemory(), config));
        pipeline.setFetchStage(new Fetch(new uint32(stringToUint32(config.getProperty("entry_point"))), pipeline.getInstructionMemory()));
        pipeline.setRegisterSet(new RegisterSet());
        pipeline.setDecodeStage(new Decode(pipeline.getRegisterSet()));
        pipeline.setExecuteStage(new Execute());
        pipeline.setBranchPredictionModule(new BranchPredictionModule(config));
        pipeline.setMemoryStage(new Memory(pipeline.getDataMemory()));
        pipeline.setWriteBackStage(new WriteBack(pipeline.getRegisterSet()));

/*
        if (ArchCfg.isa_type == ISAType.MIPS)
        {
            // set the output file for usage of printf
            PrintHandler print_handler = PrintHandler.getInstance();
            print_handler.setOutFileName(config.getProperty("print_file"));
        }
        else if (ArchCfg.isa_type == ISAType.DLX)
        {
            // set handler for printf and file management
            DLXTrapHandler trap_handler = DLXTrapHandler.getInstance();
            trap_handler.setMemory(pipeline.getMainMemory());

            // not used, since DLX does not need a config file
            //trap_handler.setOutFileName(config.getProperty("print_file"));
        }
*/
		RISCVSyscallHandler syscall_handler = RISCVSyscallHandler.getInstance();
        syscall_handler.setMemory(pipeline.getMainMemory());

        // Obtain the statistics object
        stat = Statistics.getInstance();
        stat.setConfig(config);

        // Latches:
        pipeline.setFetchDecodeLatch(new LinkedList<FetchDecodeData>());
        pipeline.setDecodeExecuteLatch(new LinkedList<DecodeExecuteData>());
        pipeline.setBranchPredictionFetchLatch(new LinkedList<BranchPredictionModuleFetchData>());
        pipeline.setBranchPredictionExecuteLatch(new LinkedList<BranchPredictionModuleExecuteData>());
        pipeline.setExecuteMemoryLatch(new LinkedList<ExecuteMemoryData>());
        pipeline.setExecuteFetchLatch(new LinkedList<ExecuteFetchData>());
        pipeline.setExecuteBranchPredictionLatch(new LinkedList<ExecuteBranchPredictionData>());
        pipeline.setMemoryWriteBackLatch(new LinkedList<MemoryWritebackData>());
        pipeline.setWriteBackLatch(new LinkedList<WriteBackData>());

        pipeline.getFetchStage().setInputLatches(pipeline.getExecuteFetchLatch(), pipeline.getBranchPredictionFetchLatch());

        pipeline.getDecodeStage().setInputLatch(pipeline.getFetchDecodeLatch());

        pipeline.getExecuteStage().setInputLatches(pipeline.getDecodeExecuteLatch(), pipeline.getBranchPredictionExecuteLatch());
        pipeline.getExecuteStage().setForwardingLatches(pipeline.getExecuteMemoryLatch(), pipeline.getMemoryWriteBackLatch(), pipeline.getWriteBackLatch());

        pipeline.getBranchPredictionModule().setInputLatches(pipeline.getExecuteBranchPredictionLatch(), pipeline.getFetchDecodeLatch());

        pipeline.getMemoryStage().setInputLatch(pipeline.getExecuteMemoryLatch());

        pipeline.getWriteBackStage().setInputLatch(pipeline.getMemoryWriteBackLatch());

        pipeline.getRegisterSet().setStackPointer(new uint32(0));

        initializePipelineLatches();
        ClockCycleLog.log.clear();
        ClockCycleLog.code.clear();
    }

    public RiscVividSimulator(File args) throws PipelineException
    {
        this(args.getAbsolutePath());
    }

    public void step() throws PipelineException
    {
        if (clock_cycle < sim_cycles && !caught_break)
        {
            logger.debug("-------------------");
            logger.debug("Cycle " + clock_cycle + " start");
            logger.debug("-------------------");

            caught_break = simulateCycle();

            logger.debug("-------------------");
            logger.debug("Cycle " + clock_cycle + " end");
            logger.debug("-------------------");
            stat.countCycle();

            HashMap<uint32, String> h = new HashMap<>();
            h.put(getPipeline().getFetchDecodeLatch().element().getPc(), GUI_CONST.FETCH);
            h.put(getPipeline().getDecodeExecuteLatch().element().getPc(), GUI_CONST.DECODE);
            h.put(getPipeline().getExecuteMemoryLatch().element().getPc(), GUI_CONST.EXECUTE);
            h.put(getPipeline().getMemoryWriteBackLatch().element().getPc(), GUI_CONST.MEMORY);
            h.put(getPipeline().getWriteBackLatch().element().getPc(), GUI_CONST.WRITEBACK);
            ClockCycleLog.log.add(h);
            ClockCycleLog.code.add(getPipeline().getFetchDecodeLatch().element().getPc());
        }
        else if (caught_break)
        {
            LoggerConfigurator.getInstance().setLogLevel(Level.DEBUG);

            logger.info("Caught break instruction - stopping simulation.");
            System.out.println("Caught break instruction after " + stat.getCycles() + " cycles. Stopped simulation.");

            // -print out selected memory
            // -check assumptions of configuration file
            finalizeSimulation(config, stat);
            finished = true;
        }
        else
        {
            LoggerConfigurator.getInstance().setLogLevel(Level.DEBUG);

            logger.info("Run to maximum cycle count ("+sim_cycles+")- stopping.");
            System.out.println("Run to maximum cycle count (" + sim_cycles + ") stopping.");

            finalizeSimulation(config, stat);
            finished = true;
        }

        clock_cycle++;
    }

    /**
     * Simulates one cycle of the pipeline
     *
     * @return true if a break instruction was caught and the simulation shall
     * be stopped, else false.
     * @throws DecodeStageException
     */
    public boolean simulateCycle() throws PipelineException
    {
        boolean caught_break = false;

        Queue<FetchDecodeData> fetch_decode_latch = pipeline.getFetchDecodeLatch();
        Queue<DecodeExecuteData> decode_execute_latch = pipeline.getDecodeExecuteLatch();
        Queue<BranchPredictionModuleFetchData> branchprediction_fetch_latch = pipeline.getBranchPredictionFetchLatch();
        Queue<BranchPredictionModuleExecuteData> branchprediction_execute_latch = pipeline.getBranchPredictionExecuteLatch();
        Queue<ExecuteMemoryData> execute_memory_latch = pipeline.getExecuteMemoryLatch();
        Queue<ExecuteFetchData> execute_fetch_latch = pipeline.getExecuteFetchLatch();
        Queue<ExecuteBranchPredictionData> execute_branchprediction_latch = pipeline.getExecuteBranchPredictionLatch();
        Queue<MemoryWritebackData> memory_writeback_latch = pipeline.getMemoryWriteBackLatch();
        Queue<WriteBackData> writeback_latch = pipeline.getWriteBackLatch();

        // Pipeline stage output data objects:
        FetchOutputData fod;
        DecodeOutputData dod;
        BranchPredictionModuleOutputData bpmod;
        ExecuteOutputData eod;
        MemoryOutputData mod;
        WritebackOutputData wod;

        // there is only one entry in the latch!
        if (fetch_decode_latch.size() != 1)
        {
            throw new PipelineException("Wrong number of entries in fetch/decode latch: " + fetch_decode_latch.size());
        }
        if (decode_execute_latch.size() != 1)
        {
            throw new PipelineException("Wrong number of entries in decode/execute latch: " + decode_execute_latch.size());
        }
        if (branchprediction_fetch_latch.size() != 1)
        {
            throw new PipelineException("Wrong number of entries in branchprediction/fetch latch: " + branchprediction_fetch_latch.size());
        }
        if (branchprediction_execute_latch.size() != 1)
        {
            throw new PipelineException("Wrong number of entries in branchprediction/execute latch: " + branchprediction_execute_latch.size());
        }
        if (execute_memory_latch.size() != 1)
        {
            throw new PipelineException("Wrong number of entries in execute/memory latch: " + execute_memory_latch.size());
        }

        if (ArchCfg.isa_type == ISAType.MIPS)
        {
            if (execute_fetch_latch.size() != 1)
            {
                throw new PipelineException("Wrong number of entries in execute/fetch latch: " + execute_fetch_latch.size());
            }
        }
        else if (ArchCfg.isa_type == ISAType.DLX)
        {
            if (ArchCfg.use_forwarding == false)
            {
                // THE DLX pipeline has a delay of 2 cycles between execute and fetch.
                // Such that 3 bubbles are between the branch instruction and its target instruction.
                if (execute_fetch_latch.size() != 2)
                {
                    throw new PipelineException("Wrong number of entries in execute/fetch latch: " + execute_fetch_latch.size());
                }
            }
            else
            {
                if (execute_fetch_latch.size() != 1)
                {
                    throw new PipelineException("Wrong number of entries in execute/fetch latch: " + execute_fetch_latch.size());
                }
            }
        }
        else
        {
            throw new PipelineException("Unknown ISA: " + ArchCfg.isa_type);
        }


        if (execute_branchprediction_latch.size() != 1)
        {
            throw new PipelineException("Wrong number of entries in execute/branchprediction latch: " + execute_branchprediction_latch.size());
        }
        if (memory_writeback_latch.size() != 1)
        {
            throw new PipelineException("Wrong number of entries in memory/writeback latch: " + memory_writeback_latch.size());
        }
        if (writeback_latch.size() != 1)
        {
            throw new PipelineException("Wrong number of entries in writeback latch: " + writeback_latch.size());
        }

        boolean stall = false;

        // FETCH STAGE
        // flush the decode on jump
        // flush the execute when a conditional "likely" branch is not taken
        fod = pipeline.getFetchStage().doCycle();
        // FETCH STAGE

        // LATCH
        if (fod.getFlush()[PipelineConstants.DECODE_STAGE])
        {
            logger.debug("Flushed DECODE PC: " 
            	+ fetch_decode_latch.element().getPc().getValueAsHexString() + " " 
            	+ fetch_decode_latch.element().getInstr().getValueAsHexString());
            fetch_decode_latch.element().flush();
        }

        // DECODE STAGE
        dod = pipeline.getDecodeStage().doCycle();
        // DECODE STAGE

        // LATCH
        if (fod.getFlush()[PipelineConstants.EXECUTE_STAGE])
        {
            logger.debug("Flushed EXECUTE PC: "
            	+ decode_execute_latch.element().getPc().getValueAsHexString() + " "
            	+ decode_execute_latch.element().getInst().toString());
            decode_execute_latch.element().flush();
        }

        // EXECUTE STAGE
        eod = pipeline.getExecuteStage().doCycle();
        // EXECUTE STAGE

        // LATCH

        // BRANCH PREDICTOR MODULE: lookup for jump target and update prediction tables
        bpmod = pipeline.getBranchPredictionModule().doCycle();
        // BRANCH PREDICTOR MODULE: lookup for jump target and update prediction tables

        // MEMORY STAGE
        mod = pipeline.getMemoryStage().doCycle();
        // MEMORY STAGE

        // LATCH

        // WRITE BACK STAGE
        wod = pipeline.getWriteBackStage().doCycle();
        caught_break = wod.getCaughtBreak();
        // WRITE BACK STAGE

        if (!stall)
        {
            // in case the execute determines that an instruction requires to
        	// forward a value from a load that is the direct predecessor
            // the a bubble needs to be inserted, since no forwarding is 
        	// possible in the load delay slot
            if (eod.getStall()[PipelineConstants.FETCH_STAGE]
            	&& eod.getStall()[PipelineConstants.DECODE_STAGE]
            	&& eod.getStall()[PipelineConstants.EXECUTE_STAGE])
            {
                logger.debug("Stalling IF, ID, and EX because of load dependency for PC: "
                	+ decode_execute_latch.element().getPc().getValueAsHexString());
                // leave FETCH, DECODE, and EXECUTE untouched

                // let the other latches running
                memory_writeback_latch.remove();
                writeback_latch.remove();

                memory_writeback_latch.add(mod.getMwd());
                writeback_latch.add(wod.getWbd());

                // kick out the memory load instruction (since it was executed)
                execute_memory_latch.element().flush();
            }
            else
            {
                // remove input from latches
                fetch_decode_latch.remove();
                decode_execute_latch.remove();
                branchprediction_fetch_latch.remove();
                branchprediction_execute_latch.remove();
                execute_memory_latch.remove();
                execute_fetch_latch.remove();
                execute_branchprediction_latch.remove();
                memory_writeback_latch.remove();
                writeback_latch.remove();

                // push output into latches
                fetch_decode_latch.add(fod.getFdd());
                decode_execute_latch.add(dod.getDed());
                branchprediction_fetch_latch.add(bpmod.getBpmfd());
                branchprediction_execute_latch.add(bpmod.getBpmed());
                execute_memory_latch.add(eod.getEmd());
                execute_fetch_latch.add(eod.getEfd());
                execute_branchprediction_latch.add(eod.getEbd());
                memory_writeback_latch.add(mod.getMwd());
                writeback_latch.add(wod.getWbd());

                // increase PC synchronously and if only if the FETCH is not stalled
                pipeline.getFetchStage().increasePC();
            }
        }

        return caught_break;
    }

    public int getSimCycles()
    {
        return this.sim_cycles;
    }

    public int getCurrentCycle()
    {
        return this.clock_cycle;
    }

    private void initializePipelineLatches()
    {
        Queue<ExecuteFetchData> efl = pipeline.getExecuteFetchLatch();
        Queue<FetchDecodeData> fdl = pipeline.getFetchDecodeLatch();
        Queue<DecodeExecuteData> del = pipeline.getDecodeExecuteLatch();
        Queue<BranchPredictionModuleFetchData> bpmfl = pipeline.getBranchPredictionFetchLatch();
        Queue<BranchPredictionModuleExecuteData> bpmel = pipeline.getBranchPredictionExecuteLatch();
        Queue<ExecuteMemoryData> eml = pipeline.getExecuteMemoryLatch();
        Queue<ExecuteBranchPredictionData> ebl = pipeline.getExecuteBranchPredictionLatch();
        Queue<MemoryWritebackData> mwl = pipeline.getMemoryWriteBackLatch();
        Queue<WriteBackData> wbl = pipeline.getWriteBackLatch();

        Decode d = new Decode(null);

        uint32 zero = new uint32(0x0);
        uint32[] zeros = new uint32[2];
        zeros[0] = zero;
        zeros[1] = zero;
        Instruction bubble;
        try
        {
            bubble = d.decodeInstr(PipelineConstants.PIPELINE_BUBBLE_INSTR);
        }
        catch (PipelineException e)
        {
            e.printStackTrace();
            bubble = new Instruction(zero);
        }

        // add 1 bubbles into fetch stage (used for jumps)
        ExecuteFetchData efd = new ExecuteFetchData(bubble, zero, zero, false, false);

        efl.add(efd);
        if ((ArchCfg.isa_type == ISAType.DLX) && (ArchCfg.use_forwarding == false))
        {
            // THE DLX pipeline has a delay of 2 cycles between execute and fetch.
            // Such that 3 bubbles are between the branch instruction and its target instruction.
            efl.add(efd);
        }

        // add 1 bubble into decode stage
        FetchDecodeData fdd = new FetchDecodeData();

        fdl.add(fdd);

        // add 1 bubble into execute stage
        DecodeExecuteData ded = new DecodeExecuteData(bubble, zero, zero, zero, zero, zero, zero);

        del.add(ded);

        // add 1 bubble into branch predictor decision
        BranchPredictionModuleFetchData bpmfd = new BranchPredictionModuleFetchData(false, zero, zero);

        bpmfl.add(bpmfd);

        // add 1 bubble into branch predictor decision
        BranchPredictionModuleExecuteData bpmed = new BranchPredictionModuleExecuteData(false, zero, zero);

        bpmel.add(bpmed);

        // add 1 bubble into memory stage
        ExecuteMemoryData emd = new ExecuteMemoryData(bubble, zero, zeros, zero, false);

        eml.add(emd);

        // add 1 bubble into the branch prediction module
        ExecuteBranchPredictionData ebd = new ExecuteBranchPredictionData(bubble, zero, zero, false);

        ebl.add(ebd);

        // add 1 bubble into write back stage
        MemoryWritebackData mwd = new MemoryWritebackData(bubble, zero, zeros, zero, false);

        mwl.add(mwd);

        // add 1 bubble into write back out buffer stage
        WriteBackData wbd = new WriteBackData(bubble, zero, zeros, zero);

        wbl.add(wbd);
    }

    private void finalizeSimulation(Properties config, Statistics stat)
    {
        // TODO specify in config what is actually to be checked after simulation

        if (config.containsKey("dump_memory_start") && config.containsKey("dump_memory_end"))
        {
            try
            {
                pipeline.getMainMemory().dumpMemory(stringToUint32(config.getProperty("dump_memory_start")), stringToUint32(config.getProperty("dump_memory_end")));
            }
            catch (MemoryException e)
            {
                e.printStackTrace();
            }
        }

        if (config.containsKey("dump_registers") && (stringToUint32(config.getProperty("dump_registers")).getValue() == 1))
        {
            pipeline.getRegisterSet().printContent();
        }

        for (short i = 0; i < 32; i++)
        {
            checkRegisterValues(i, config, pipeline.getRegisterSet());
        }

        checkSpecialRegisterValues(config, pipeline.getRegisterSet());

        // print out the stats from the simulation run
        stat.printStats();
    }

    private void setDefaultConfigParameters(Properties config) throws PipelineException
    {
        if (!config.containsKey("file"))
        {
            throw new PipelineException("Error: the \"file\" property is missing in the configuration file.");
        }

        if (!config.containsKey("entry_point"))
        {
            throw new PipelineException("Error: the \"entry_point\" property is missing in the configuration file.");
        }

        if (!config.containsKey("code_start_addr"))
        {
//            config.setProperty("code_start_addr", "0x00400174");
            throw new PipelineException("Error: the \"code_start_addr\" property is missing in the configuration file.");
        }

        if (!config.containsKey("cycles"))
        {
            config.setProperty("cycles", "10000");
        }

        if (!config.containsKey("log4j"))
        {
            config.setProperty("log4j", "log4j.properties");
        }

        if (!config.containsKey("print_file"))
        {
            config.setProperty("print_file", "printf.out");
        }

        if (!config.containsKey("memory_latency"))
        {
            config.setProperty("memory_latency", "0");
        }

        if (!config.containsKey("icache_use"))
        {
            config.setProperty("icache_use", "0");
        }

        if (!config.containsKey("dcache_use"))
        {
            config.setProperty("dcache_use", "0");
        }

        if (!config.containsKey("isa_type"))
        {
            // default ISA is MIPS
            config.setProperty("isa_type", "MIPS");
        }

        if (!config.containsKey("use_forwarding"))
        {
            if (ArchCfg.stringToISAType(config.getProperty("isa_type")) == ISAType.MIPS)
            {
                config.setProperty("use_forwarding", "TRUE");
            }
            else if (ArchCfg.stringToISAType(config.getProperty("isa_type")) == ISAType.DLX)
            {
                config.setProperty("use_forwarding", "FALSE");
            }
        }

        if (!config.containsKey("use_load_stall_bubble"))
        {
            if (ArchCfg.stringToISAType(config.getProperty("isa_type")) == ISAType.MIPS)
            {
                config.setProperty("use_load_stall_bubble", "TRUE");
            }
            else if (ArchCfg.stringToISAType(config.getProperty("isa_type")) == ISAType.DLX)
            {
                config.setProperty("use_load_stall_bubble", "FALSE");
            }
        }

        if (!config.containsKey("no_branch_delay_slot"))
        	config.setProperty("no_branch_delay_slot", "TRUE");
        	
    }

    private void checkRegisterValues(short i, Properties config, RegisterSet reg_set)
    {
        if (config.containsKey("assert_reg_" + i + "_value"))
        {
            if (stringToUint32(config.getProperty("assert_reg_" + i + "_value")).getValue() != reg_set.read(new uint8(i)).getValue())
            {
                logger.warn("Register " + i + " does not has the expected value: " + stringToUint32(config.getProperty("assert_reg_" + i + "_value")).getValueAsHexString() + " != " + reg_set.read(new uint8(i)).getValueAsHexString());
            }
            else
            {
                logger.info("Register " + i + " has the expected value: " + stringToUint32(config.getProperty("assert_reg_" + i + "_value")).getValueAsHexString() + " == " + reg_set.read(new uint8(i)).getValueAsHexString());
            }
        }
    }

    private void checkSpecialRegisterValues(Properties config, RegisterSet reg_set)
    {
        if (config.containsKey("assert_reg_LO_value"))
        {
            if (stringToUint32(config.getProperty("assert_reg_LO_value")).getValue() != reg_set.read_SP(SpecialRegisters.LO).getValue())
            {
                logger.warn("Register LO does not has the expected value: " + stringToUint32(config.getProperty("assert_reg_LO_value")).getValueAsHexString() + " != " + reg_set.read_SP(SpecialRegisters.LO).getValueAsHexString());
            }
            else
            {
                logger.info("Register LO has the expected value: " + stringToUint32(config.getProperty("assert_reg_LO_value")).getValueAsHexString() + " == " + reg_set.read_SP(SpecialRegisters.LO).getValueAsHexString());
            }
        }

        if (config.containsKey("assert_reg_HI_value"))
        {
            if (stringToUint32(config.getProperty("assert_reg_HI_value")).getValue() != reg_set.read_SP(SpecialRegisters.HI).getValue())
            {
                logger.warn("Register HI does not has the expected value: " + stringToUint32(config.getProperty("assert_reg_HI_value")).getValueAsHexString() + " != " + reg_set.read_SP(SpecialRegisters.HI).getValueAsHexString());
            }
            else
            {
                logger.info("Register HI has the expected value: " + stringToUint32(config.getProperty("assert_reg_HI_value")).getValueAsHexString() + " == " + reg_set.read_SP(SpecialRegisters.HI).getValueAsHexString());
            }
        }
    }

    /**
     * Converts a string (hex or decimal) into a uint32 i.e. only the lowest
     * 32bit are used It is needed, because the hex string 0xffffffff cannot be
     * converted by Integer.decode(), since the value is larger than
     * Integer.MAX_VALUE.
     *
     * @param s String to parse
     * @return The parsed uint32 number
     */
    private uint32 stringToUint32(String s)
    {
        return new uint32(Long.decode(s).intValue());
    }

    public PipelineContainer getPipeline()
    {
        return pipeline;
    }

    public boolean isFinished()
    {
        return finished;
    }

    public Properties getConfig()
    {
        return config;
    }

    public void stopSimulation(boolean error)
    {
        if (error)
        {
            logger.error("Simulation stopped on error.");
        }
        else
        {
            logger.info("Simulation stopped by user.");
        }
        finished = true;
    }

}
