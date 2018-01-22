package seminar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.*;
import ilog.cplex.IloCplex.UnknownObjectException;

public class MaxModel {

	private static final int timelim = 300;  
	private static final int C = 20; 
	
	
	private IloCplex cplex;
	private ArrayList<Pilot> pilots; 
	private ArrayList<Event> events; 
	private ArrayList<Plane> planes;
	private int I; 
	private int J; 
	private int K; 
	private int T;
	
	private IloNumVar[][][] X;
	private IloNumVar[][] Y;
	private IloNumVar[][][] V; 
	private IloNumVar[][] Z; 
	
	private double beta ;
	
//	private HashMap<HashMap<HashMap<IloNumVar, Integer>, Integer>,Integer> varMapX; 
//	private HashMap<Integer, HashMap<Integer, HashMap<Integer, IloNumVar>>> itemMapX; 
//	private HashMap<Integer, HashMap<Integer, IloNumVar>> itemMapY; 
//	private HashMap<HashMap<IloNumVar, Integer>, Integer> varMapY; 
//	private HashMap<Integer, HashMap<Integer, IloNumVar>> itemMapV; 
//	private HashMap<HashMap<IloNumVar, Integer>, Integer> varMapV; 
	
	public MaxModel(ArrayList<Pilot> pilotList, ArrayList<Event> eventList, ArrayList<Plane> planeList, int lengthTimeFrame, double valueBeta) throws IloException, objectNotFoundException{
		pilots = pilotList; 
		events = eventList; 
		planes = planeList; 
		I = pilots.size();
		J = events.size(); 
		K = planes.size();
		T = lengthTimeFrame; 
		beta = valueBeta; 
//		varMapX = new HashMap<HashMap<HashMap<IloNumVar, Integer>, Integer>, Integer>();  
//		itemMapX = new HashMap<Integer, HashMap<Integer, HashMap<Integer, IloNumVar>>>();  
//		itemMapY = new HashMap<Integer, HashMap<Integer, IloNumVar>>(); 
//		varMapY = new HashMap<HashMap<IloNumVar, Integer>, Integer>(); 
//		itemMapV = new HashMap<Integer, HashMap<Integer, IloNumVar>>(); 
//		varMapV = new HashMap<HashMap<IloNumVar, Integer>, Integer>();
		X = new IloNumVar[I][J][T];
		Y = new IloNumVar[K][T];
		V = new IloNumVar[C][J][T]; 
		Z = new IloNumVar[I][J]; 
		
		cplex = new IloCplex();
		cplex.setParam(	IloCplex.Param.TimeLimit, timelim); 
		
		initVars();
		initCompleteTraining(); 
		initMax1Event(); 
		initNrPlanesIsNrPilots(); 
		initNrPilotsPerEvent(); 
		initRequiredMachine(); 
		initObjective();
	}
	
	// initiates binary variables with two indices : x_ij /in {0,1}
	// initiates variable with value between 1 and n-1: u_i  
	public void initVars() throws IloException{
		// create X 
		for (int i = 0; i < I; i++) {
			for (int j = 0; j<J; j++) {
				for (int t= 0; t<T; t++) {
					IloNumVar varX = cplex.boolVar();
					X[i][j][t] = varX; 
				}
			}
		}
		// create Y 
		for (int k = 0; k <K; k++) {
			for (int t = 0; t < T; t++) {
				IloNumVar varY = cplex.boolVar();
				Y[k][t] = varY;
			}
		}
		// create V 
		for (int c = 0; c < C; c++) {
			for (int j = 0; j < J; j++) {
				for (int t = 0; t < T; t++) {
					//IloNumVar varV = cplex.intVar(0,25);
					IloNumVar varV = cplex.boolVar(); 
					V[c][j][t] = varV;
				}
			}	
		}
		// create Z 
		for (int i = 0; i < I; i++) {
			for (int j = 0; j < J; j++) {
				IloNumVar varZ = cplex.boolVar(); 
					Z[i][j] = varZ;
			}
		}
		
		
		
//		// create X 
//		for(int i = 1; i<=pilots.size(); i++)
//		{
//			HashMap<Integer, HashMap<Integer, IloNumVar>> itemMapTemp2 = new HashMap<Integer, HashMap<Integer, IloNumVar>>(); 
//			HashMap<HashMap<IloNumVar, Integer>, Integer> varMapTemp2 = new HashMap<HashMap<IloNumVar, Integer>, Integer>(); 
// 			for (int j = 1; j<=events.size(); j++){
// 				HashMap<Integer, IloNumVar> itemMapTemp = new HashMap<Integer, IloNumVar>(); 
// 				HashMap<IloNumVar, Integer> varMapTemp = new HashMap<IloNumVar, Integer>();
// 				for (int t = 1; t<=T; t++) {
// 					IloNumVar varX = cplex.boolVar();
// 					itemMapTemp.put(t,varX);
// 					varMapTemp.put(varX,t);
// 				}
// 				itemMapTemp2.put(j, itemMapTemp);
// 				varMapTemp2.put(varMapTemp, j);
//			}	
// 			varMapX.put(varMapTemp2,i);
// 			itemMapX.put(i, itemMapTemp2);
//		}
//		
//		// Create Y 
//		for(int k = 1; k<=planes.size(); k++)
//		{ 
//			HashMap<Integer, IloNumVar> itemMapTemp = new HashMap<Integer, IloNumVar>(); 
//			HashMap<IloNumVar, Integer> varMapTemp = new HashMap<IloNumVar, Integer>();
//			for (int t2 = 1; t2<=T; t2++) {
//					IloNumVar varY = cplex.boolVar();
//					itemMapTemp.put(t2,varY);
//					varMapTemp.put(varY,t2);
//				}
// 			varMapY.put(varMapTemp,k);
// 			itemMapY.put(k, itemMapTemp);
//		}
//		
//		// Create V
//		for(int j2 = 1; j2<=events.size(); j2++)
//		{ 
//			HashMap<Integer, IloNumVar> itemMapTemp = new HashMap<Integer, IloNumVar>(); 
//			HashMap<IloNumVar, Integer> varMapTemp = new HashMap<IloNumVar, Integer>();
//			for (int t3 = 1; t3<=T; t3++) {
//					IloNumVar varV = cplex.numVar(1,5);
//					itemMapTemp.put(t3,varV);
//					varMapTemp.put(varV,t3);
//				}
// 			varMapV.put(varMapTemp,j2);
// 			itemMapV.put(j2, itemMapTemp);
//		}
	}
	
	// C1: ensures completion training
	public void initCompleteTraining() throws IloException{
		for(int i = 0; i<I; i++) {
			for (int j = 0; j < J; j++) {
				IloNumExpr expr= cplex.numExpr();
				for (int t= 0; t<T; t++) { 
					IloNumVar var= X[i][j][t];
					expr = cplex.sum(expr, var);
				}
				IloNumExpr expr2 = cplex.numExpr(); 
				IloNumVar var2 = Z[i][j]; 
				expr2 = cplex.sum(expr2, var2);
				expr2 = cplex.sum(expr2, pilots.get(i).getQij(j));
				if (expr != null && expr2 != null){
				cplex.addLe(expr, expr2);	
				}
			}
		}
	}
	
	// C2: ensures max 1 event per time t 
	public void initMax1Event() throws IloException{
		for (int i =0 ; i<I ; i++) {
			for (int t=0; t<T; t++) {
				IloNumExpr expr = cplex.numExpr();
				for (int j = 0 ; j<J; j++) {
					IloNumVar var= X[i][j][t];
					expr = cplex.sum(expr, var);
				}
				if (expr != null){
				cplex.addLe(expr, 1);	
				}
			}
		}
	}
	
	//C5
	public void initNrPlanesIsNrPilots() throws IloException{
		for (int t = 0; t<T; t++) {
			IloNumExpr expr = cplex.numExpr();
			for (int k = 0 ; k< K; k++) {
				IloNumVar var= Y[k][t];
				expr = cplex.sum(expr, var); 
			}
			IloNumExpr expr2 = cplex.numExpr();
			for(int i = 0; i< I; i++) {
				for (int j = 0; j < J; j++) {
					IloNumVar var= X[i][j][t];
					expr2 = cplex.sum(expr2, var);
				}
			}
			if (expr != null && expr2 != null){
			cplex.addEq(expr, expr2);	
			}
		}
	}
	
	//C6
	public void initNrPilotsPerEvent()throws IloException{
		for (int j = 0 ; j< J ; j++) {
			for (int t = 0 ; t<T; t++) {
				IloNumExpr expr = cplex.numExpr();
				for (int i = 0; i < I; i++) {
					IloNumVar var= X[i][j][t];
					expr = cplex.sum(expr, var);
				}
				IloNumExpr expr2 = cplex.numExpr();
				for (int c = 0; c < C; c++) {
					IloNumVar var2 = V[c][j][t]; 
					expr2 = cplex.sum(expr2, var2);
				}
				expr2 = cplex.prod(expr2, events.get(j).getN());
				
				if (expr != null && expr2 != null){
					cplex.addEq(expr, expr2);	
				}
			}
		}
	}
	
	//C7
	public void initRequiredMachine() throws IloException{
		for (int t = 0; t < T; t++) {
			// left side
			IloNumExpr expr = cplex.numExpr();
			for (int k = 0; k < K ; k++) {
				IloNumExpr term = cplex.prod(planes.get(k).getType(), Y[k][t]);
				expr = cplex.sum(expr, term); 
			}
			// right side 
			IloNumExpr expr2 = cplex.numExpr();
			for (int j = 0; j<J; j++) {
				IloNumExpr expr2temp = cplex.numExpr();
				for (int i = 0; i<I; i++) {
					IloNumVar var = X[i][j][t];
					expr2temp = cplex.sum(expr2temp, var); 
				}
				expr2temp = cplex.prod(expr2temp, events.get(j).getR());
				expr2 = cplex.sum(expr2, expr2temp); 
			}
			if (expr != null && expr2 != null){
				cplex.addEq(expr, expr2);	
			}
		}
	}
	
	public void initObjective() throws IloException, objectNotFoundException{
	IloNumExpr expr= cplex.linearNumExpr();
	
	for(int i = 0; i < I; i++) {
		for (int j = 0; j< J; j++) {
			for (int t = 0; t< T; t++) { 
				IloNumExpr term = cplex.prod(events.get(j).getN(), X[i][j][t] );
				expr = cplex.sum(expr, X[i][j][t]);
			}
			IloNumExpr term2 = cplex.prod(beta, Z[i][j]); 
			expr = cplex.diff(expr, term2);
		}
	}
	cplex.addMaximize(expr);
	}
	
	public boolean solve() throws IloException{
		return cplex.solve();
	}
	
	public double getSolution() throws IloException{
		return cplex.getObjValue(); 
	}
	
	public int getObjectiveX() throws UnknownObjectException, IloException {
		int obj = 0; 
		for (int i = 0; i<I; i++) {
			for (int j = 0; j<J; j++) {
				for (int t = 0; t<T; t++) {
					obj += cplex.getValue(X[i][j][t]); 
				}
			}
		}
		return obj; 
	}
	
	public void printSolution() throws IloException{
		System.out.println("Solution value: "+cplex.getObjValue());
		for (int j=0; j<J; j++) {
			for (int t=0; t<T; t++) {
				IloNumVar var = X[1][j][t];
				System.out.println("Pilot 1, event " + j + " and time " + t+ " : " + cplex.getValue(var));;
			}
		}
//		for(String i: itemMapX.keySet())
//		{
//			for (String j: itemMapX.keySet()){
//				if (!i.equals(j)){
//					IloNumVar var = itemMapX.get(i).get(j);
//					System.out.println("Agent "+i+ "and " + j+ "(X): " +cplex.getValue(var));
//				}
//			}
//		}
	}
	
	public ArrayList<Pilot> updateQij() throws UnknownObjectException, IloException, objectNotFoundException{
		for (Pilot i: pilots) {
			int[] q = i.getQij(); 
			int nri = i.getNr(); 
			for (int j = 0; j<J; j++) {
				int sumT = 0; 
				for (int t = 0; t< T; t++) {
					sumT += cplex.getValue(X[nri][j][t]); 
				}
				if (q[j]-sumT  >= 0) {
					q[j] = q[j]- sumT;	
				}
				else {
					q[j]= 0; 
				}
				 
			}
			i.addQj(q);
		}
		return pilots; 
	}
	
	public void closeModel(){
		cplex.end(); 
	}
}

