package crashfilter.va.regs;

import crashfilter.va.MLocAnalysis.IValue;


public class ActualReg implements IRegister, IValue
{
	String rname;
	
	public static ActualReg STACK = new ActualReg("stack");
	public static ActualReg HEAP = new ActualReg("heap");
	public static ActualReg OLDEBP = new ActualReg("oldebp");
	public static ActualReg RETADDR = new ActualReg("retAddr");
	
	public static ActualReg EBP = new ActualReg("ebp");
	public static ActualReg ESP = new ActualReg("esp"); 
	public static ActualReg EAX = new ActualReg("eax");
	public static ActualReg EBX = new ActualReg("ebx");
	public static ActualReg ECX = new ActualReg("ecx"); 
	public static ActualReg EDX = new ActualReg("edx");
	public static ActualReg EDI = new ActualReg("edi");
	public static ActualReg ESI = new ActualReg("esi");
	public static ActualReg OF = new ActualReg("OF");
	public static ActualReg ZF = new ActualReg("ZF");
	public static ActualReg CF = new ActualReg("CF");
	public static ActualReg SF = new ActualReg("SF");

	
	//for Arm
	public static ActualReg R1 = new ActualReg("R1");
	public static ActualReg R2= new ActualReg("R2");
	public static ActualReg R3 = new ActualReg("R3");
	public static ActualReg R4 = new ActualReg("R4");
	public static ActualReg R5 = new ActualReg("R5");
	public static ActualReg R6 = new ActualReg("R6");
	public static ActualReg R7 = new ActualReg("R7");
	public static ActualReg R8 = new ActualReg("R8");
	public static ActualReg R9 = new ActualReg("R9");
	public static ActualReg R10 = new ActualReg("R10");
	public static ActualReg R11 = new ActualReg("R11");
	public static ActualReg R12 = new ActualReg("R12");
	public static ActualReg R13 = new ActualReg("R13");
	public static ActualReg R14 = new ActualReg("R14");
	public static ActualReg R15 = new ActualReg("R15");
	
	public static ActualReg OLD_R1 = new ActualReg("OLD_R1");
	public static ActualReg OLD_R2 = new ActualReg("OLD_R2");
	public static ActualReg OLD_R3 = new ActualReg("OLD_R3");
	public static ActualReg OLD_R4 = new ActualReg("OLD_R4");
	public static ActualReg OLD_R5 = new ActualReg("OLD_R5");
	public static ActualReg OLD_R6 = new ActualReg("OLD_R6");
	public static ActualReg OLD_R7 = new ActualReg("OLD_R7");
	public static ActualReg OLD_R8 = new ActualReg("OLD_R8");
	public static ActualReg OLD_R9 = new ActualReg("OLD_R9");
	public static ActualReg OLDR_10 = new ActualReg("OLD_R10");
	public static ActualReg OLDR_11 = new ActualReg("OLD_R11");
	public static ActualReg OLDR_12 = new ActualReg("OLD_R12");
	public static ActualReg OLDR_13 = new ActualReg("OLD_R13");
	public static ActualReg OLDR_14 = new ActualReg("OLD_R14");
	public static ActualReg OLDR_15 = new ActualReg("OLD_R15");
	
	
	public static ActualReg SP = new ActualReg("SP");
	public static ActualReg LR = new ActualReg("LR");
	public static ActualReg C = new ActualReg("C");
	public static ActualReg N = new ActualReg("N");
	public static ActualReg V = new ActualReg("V");
	public static ActualReg PC = new ActualReg("PC");
	
	
	
	
	public ActualReg(String rname) { this.rname = rname;}

	@Override
	public String getRegName(){ return rname;}
	@Override
	public String toString() {	return rname;}
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return rname.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj.getClass() != ActualReg.class)
		{
			return false;
		}
		ActualReg t = (ActualReg)obj;
		boolean bool =  this.rname.equals(t.rname);
		return bool;
	}
}

