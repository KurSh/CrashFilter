package staticAnalysis;
import java.io.Console;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.security.zynamics.binnavi.API.gui.LogConsole;
import com.google.security.zynamics.binnavi.API.reil.ReilInstruction;
import com.google.security.zynamics.binnavi.API.reil.mono.DefaultStateVector;
import com.google.security.zynamics.binnavi.API.reil.mono.DownWalker;
import com.google.security.zynamics.binnavi.API.reil.mono.IInfluencingState;
import com.google.security.zynamics.binnavi.API.reil.mono.ILattice;
import com.google.security.zynamics.binnavi.API.reil.mono.ILatticeElement;
import com.google.security.zynamics.binnavi.API.reil.mono.ILatticeGraph;
import com.google.security.zynamics.binnavi.API.reil.mono.IStateVector;
import com.google.security.zynamics.binnavi.API.reil.mono.ITransformationProvider;
import com.google.security.zynamics.binnavi.API.reil.mono.InstructionGraphNode;
import com.google.security.zynamics.binnavi.API.reil.mono.MonotoneSolver;

import crashfilter.va.MLocAnalysis.IValue;
import crashfilter.va.MLocAnalysis.RTable.RTable;
import crashfilter.va.MLocAnalysis.RTable.RTableLatticeElement;
import crashfilter.va.MLocAnalysis.env.Env;
import crashfilter.va.MLocAnalysis.env.EnvLatticeElement;
import crashfilter.va.memlocations.MLocException;
import crashfilter.va.memlocations.StructuredMLoc;
import crashfilter.va.memlocations.StructuredMLoc.StructuredMLocBuilder;
import crashfilter.va.regs.ActualReg;
import data.ReilInstructionResolve;
import helper.CrashSourceAdder;


public class ReachingDefinitionAnalysis {
	private ILatticeGraph<InstructionGraphNode> graph;
	IStateVector<InstructionGraphNode, RTableLatticeElement> locResult;
	IStateVector<InstructionGraphNode, EnvLatticeElement> envResult;
	Long crashAddr  =null;
	boolean monotoneChecker = true;;
	
	public ReachingDefinitionAnalysis( ILatticeGraph<InstructionGraphNode> graph , Long crashAddr){
		this.graph = graph;
		this.crashAddr = crashAddr;		
	}
	public void setLocResult(IStateVector<InstructionGraphNode, RTableLatticeElement> LocResult)
	{
		this.locResult = LocResult;	
	}
	public void setEnvResult(
			IStateVector<InstructionGraphNode, EnvLatticeElement> envResult) {
		this.envResult = envResult;
		
	}
	public class RDLatticeElement implements ILatticeElement<RDLatticeElement>{
		
		private InstructionGraphNode inst;
		private Set<InstructionGraphNode> instList = new HashSet<InstructionGraphNode>();
		private Set<InstructionGraphNode> killList = new HashSet<InstructionGraphNode>();
		
		public void setInst( InstructionGraphNode inst){
			this.inst = inst;
		}
		public InstructionGraphNode getInst(){
			return inst;
		}
		public Set<InstructionGraphNode> getInstList( ){
			return instList;
		}
		
		public Set<InstructionGraphNode> getKillList( ){
			return killList;
		}
		
		public void unionInstList(Set<InstructionGraphNode> state){
			this.instList.addAll(state);
		}
		
		public void unionKillList(Set<InstructionGraphNode> killList){
			this.killList.addAll(killList);
		}
		
		public void removeAllInstList(Set<InstructionGraphNode> instList){
			this.instList.removeAll(instList);
		}
		
		public void insertInst(InstructionGraphNode inst){
			this.instList.add(inst);
		}
		
		public void insertKill(InstructionGraphNode inst){
			this.killList.add(inst);
		}
		
		@Override
		public boolean equals(RDLatticeElement rhs) {
			if(rhs.getInstList().containsAll(instList)){
				if(instList.size() == rhs.getInstList().size())
				{
					return true;
				}
			}
			else
				; //error - it is not monotone
			return false;
		}

		@Override
		public boolean lessThan(RDLatticeElement rhs) {
			if(rhs.getInstList().containsAll(instList)){
				if(instList.size() < rhs.getInstList().size())
				{
					return true;
				}
				
			}
			else
				; //error - it is not monotone
			return false;
		}
		
		

	}
	
	//This function is used to combine states in each state positions of program.
	public class RDLattice implements ILattice<RDLatticeElement, Object>{
		 
		@Override
		public RDLatticeElement combine( List<IInfluencingState<RDLatticeElement, Object>> states ) {
			RDLatticeElement combinedState = new RDLatticeElement();
			
			//Union all the predecessor's state
			for ( IInfluencingState<RDLatticeElement, Object> state : states ){
				combinedState.unionInstList(state.getElement().getInstList());				
			}
			
			return combinedState;
		}
	}
	
	public class RDTransferFunction implements ITransformationProvider<InstructionGraphNode, RDLatticeElement>{
		@Override
		public RDLatticeElement transform(
				InstructionGraphNode node,
				RDLatticeElement currentState,
				RDLatticeElement inputState
				) {

			
			long crashSourceInstAddr = CrashSourceAdder.getInstruction(graph, crashAddr).getInstruction().getAddress().toLong();
			long nextAddr = CrashSourceAdder.getNextAddrOfCrash(graph, crashAddr);
			long nowAddr = node.getInstruction().getAddress().toLong();
			//each InstructionGraphNodes like LDM and STM, we can resolve the memory access operand using value-set analysis result
			RDLatticeElement transformedState = new RDLatticeElement();
						

			transformedState.unionInstList(inputState.getInstList());
			transformedState.removeAllInstList(currentState.getKillList());
			
			if(!(ReilInstructionResolve.resolveReilInstructionDest(node).isEmpty())){			
				transformedState.insertInst(node);
			}
			
			
			transformedState.unionKillList(currentState.getKillList());
			
			return transformedState;
		}
	}
	
	

	
	public  IStateVector<InstructionGraphNode, RDLatticeElement> initializeState(ILatticeGraph<InstructionGraphNode> graph) throws MLocException{
		
		 RDLatticeElement state;
		 IStateVector<InstructionGraphNode, RDLatticeElement> startVector = 
				 new DefaultStateVector<InstructionGraphNode, RDLatticeElement>();
		 //gathering the kill set of each instruction 
		 //After memory access analysis, we have to use the results.
		 

		 for (InstructionGraphNode defInst1 : graph.getNodes()){
			 System.out.println(defInst1);
			 state = new RDLatticeElement();
			 for (InstructionGraphNode defInst2 : graph.getNodes()){

				 //Some time later we will add VSA and have to add some code for new kill set considering memory
				 if(ReilInstructionResolve.isSameDefinition(defInst1, defInst2)){
					 state.insertKill(defInst2);
				 }
	 
			 }
			 startVector.setState(defInst1, state);
		 }

		return startVector;
	}


	public IStateVector<InstructionGraphNode, RDLatticeElement> reachingDefinitionAnalysis() throws MLocException {
//		 MessageBox.showInformation(null, "MenuPlugin test!!");
		 RDLattice lattice;
		 IStateVector<InstructionGraphNode, RDLatticeElement> startVector;
		 IStateVector<InstructionGraphNode, RDLatticeElement> endVector;
		 
		 ITransformationProvider<InstructionGraphNode, RDLatticeElement> transferFunction;
		 DownWalker<InstructionGraphNode> walker;
		 MonotoneSolver<InstructionGraphNode, RDLatticeElement, Object, RDLattice> solver;


		 lattice = new RDLattice();
		 
		 startVector = initializeState(graph);
		 transferFunction = new RDTransferFunction();
		 walker = new DownWalker<InstructionGraphNode>();
		 solver = new MonotoneSolver<InstructionGraphNode, RDLatticeElement, Object, RDLattice>(
				 graph, lattice, startVector, transferFunction, walker
				 );
		 

		 return endVector = solver.solve();			 
	}
	
	public void printRD(IStateVector<InstructionGraphNode, RDLatticeElement> endVector){	 
		
		RDLatticeElement state = null;
		 for( InstructionGraphNode inst : graph.getNodes() ){
			 state = endVector.getState(inst);
			 LogConsole.log("instruction : ");
			 LogConsole.log(inst.getInstruction().toString());
			 LogConsole.log("\n");
		 
			 for( InstructionGraphNode reachingInst : state.getInstList()){
				 LogConsole.log("\t" + reachingInst.getInstruction().toString());
				 LogConsole.log("\n");
			 }
		 }
	}
	
	
	
	
	/////  
	
	public boolean differentMemoryCheckRTable(InstructionGraphNode inst1, InstructionGraphNode inst2 )
	{
		if(isFromStackMemoryRTable(inst1) && isToHeapMemoryRTable(inst2))
		{
			return true;
		}
		if(isFromHeapMemoryRTable(inst1) && isToStackMemoryRTable(inst2))
		{
			return true;
		}
		return false;
	}
	
	private boolean isFromStackMemoryRTable(InstructionGraphNode inst)
	{
		ReilInstruction reilInst = inst.getInstruction();
		if(reilInst.getMnemonic() != "ldm")
		{
			return false;
		}
		RTableLatticeElement lle = locResult.getState(inst);
		RTable rTable = lle.getRTable();
		IValue v = rTable.checkStackOrHEapMemory( reilInst.getFirstOperand().getValue());
		if(v==null)
		{
			return false;
		}
		if(v.equals(new ActualReg("stack")))
		{
			return true;
		}
		return false;
	}
	
	private boolean isFromHeapMemoryRTable(InstructionGraphNode inst)
	{
		ReilInstruction reilInst = inst.getInstruction();
		if(reilInst.getMnemonic() != "ldm")
		{
			return false;
		}
		RTableLatticeElement lle = locResult.getState(inst);
		RTable rTable = lle.getRTable();
		IValue v = rTable.checkStackOrHEapMemory( reilInst.getFirstOperand().getValue());
		if(v==null)
		{
			return false;
		}
		if(v.equals(new ActualReg("stack")))
		{
			return true;
		}
		return false;
	}
	
	private boolean isToStackMemoryRTable(InstructionGraphNode inst)
	{
		ReilInstruction reilInst = inst.getInstruction();
		if(reilInst.getMnemonic() != "stm")
		{
			return false;
		}
		RTableLatticeElement lle = locResult.getState(inst);
		RTable rTable = lle.getRTable();
		IValue v = rTable.checkStackOrHEapMemory( reilInst.getThirdOperand().getValue());
		if(v==null)
		{
			return false;
		}
		if(v.equals(new ActualReg("stack")))
		{
			return true;
		}
		return false;
	}
	
	private boolean isToHeapMemoryRTable(InstructionGraphNode inst)
	{
		ReilInstruction reilInst = inst.getInstruction();
		if(reilInst.getMnemonic() != "stm")
		{
			return false;
		}
		RTableLatticeElement lle = locResult.getState(inst);
		RTable rTable = lle.getRTable();
		IValue v = rTable.checkStackOrHEapMemory( reilInst.getThirdOperand().getValue());
		if(v==null)
		{
			return false;
		}
		if(v.equals(new ActualReg("heap")))
		{
			return true;
		}
		return false;
	}
	
	
	
	
}
