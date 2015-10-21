/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

import jsprit.analysis.toolbox.GraphStreamViewer;
import jsprit.analysis.toolbox.Plotter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.io.VrpXMLWriter;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;
//import jsprit.util.Examples;

import java.util.Arrays;
import java.util.Collection;

// SimpleEnRoutePickupAndDeliveryExample
public class SimpleExample {
	
	public static void main(String[] args) {
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		
		/*
		 * add vehicles with its depots
		 * 4 depots with the following coordinates:
		 * (20,20), (30,40), (50,30), (60,50)
		 * 
		 * each with 1 vehicles each with a capacity of 4
		 */
		int nuOfVehicles = 1;
		int capacity = 4;
		Coordinate firstDepotCoord = Coordinate.newInstance(10, 50);
//		Coordinate second = Coordinate.newInstance(50, 10);
		        
		int depotCounter = 1;
		for(Coordinate depotCoord : Arrays.asList(firstDepotCoord)){
		    for(int i=0;i<nuOfVehicles;i++){
		        String typeId = depotCounter + "_type";
		        VehicleType vehicleType = VehicleTypeImpl.Builder.newInstance(typeId).addCapacityDimension(0,capacity).setCostPerDistance(1.0).build();
		        String vehicleId = depotCounter + "_" + (i+1) + "_vehicle";
		        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance(vehicleId);
		        vehicleBuilder.setStartLocation(Location.newInstance(depotCoord.getX(),depotCoord.getY()));  //defines the location of the vehicle and thus the depot
		        vehicleBuilder.setType(vehicleType);
		        VehicleImpl vehicle = vehicleBuilder.build();
		        vrpBuilder.addVehicle(vehicle);
		    }
		    depotCounter++;
		}
		
		/*
		 * define problem with finite fleet
		 */
		vrpBuilder.setFleetSize(FleetSize.FINITE);
			
		/*
		 * build shipments at the required locations, each with a capacity-demand of 1.
		 * 5 shipments
		 * 1: (10,50)->(50,10)
		 * 2: (100,50)->(50,10)
		 * 3: (50,10)->(100,10)
		 * 4: (50,10)->(10,10)
		 * 5: (10,10)->(100,50)
		 */
		
		Shipment shipment1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(10, 50))).setDeliveryLocation(loc(Coordinate.newInstance(50, 10))).build();
		Shipment shipment2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(100, 50))).setDeliveryLocation(loc(Coordinate.newInstance(50, 10))).build();
		Shipment shipment3 = Shipment.Builder.newInstance("3").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(50, 10))).setDeliveryLocation(loc(Coordinate.newInstance(100, 10))).build();
		Shipment shipment4 = Shipment.Builder.newInstance("4").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(50, 10))).setDeliveryLocation(loc(Coordinate.newInstance(10, 10))).build();
		Shipment shipment5 = Shipment.Builder.newInstance("5").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(10, 10))).setDeliveryLocation(loc(Coordinate.newInstance(100, 50))).build();
		
		vrpBuilder.addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4).addJob(shipment5);
		
		VehicleRoutingProblem problem = vrpBuilder.build();
		
		/*
		 * get the algorithm out-of-the-box. 
		 */
		VehicleRoutingAlgorithm algorithm = new SchrimpfFactory().createAlgorithm(problem);
		
		/*
		 * and search a solution
		 */
		Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
		
		/*
		 * get the best 
		 */
		VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
		
		/*
		 * write out problem and solution to xml-file
		 */
		new VrpXMLWriter(problem, solutions).write("output/shipment-problem-with-solution.xml");
		
		/*
		 * print nRoutes and totalCosts of bestSolution
		 */
		SolutionPrinter.print(bestSolution);
		
		/*
		 * plot problem without solution
		 */
		Plotter problemPlotter = new Plotter(problem);
		problemPlotter.plotShipments(true);
		problemPlotter.plot("output/simpleEnRoutePickupAndDeliveryExample_problem.png", "en-route pickup and delivery");
		
		/*
		 * plot problem with solution
		 */
		Plotter solutionPlotter = new Plotter(problem,Arrays.asList(Solutions.bestOf(solutions).getRoutes().iterator().next()));
		solutionPlotter.plotShipments(true);
		solutionPlotter.plot("output/simpleEnRoutePickupAndDeliveryExample_solution.png", "en-route pickup and delivery");
		
		new GraphStreamViewer(problem, bestSolution).setRenderShipments(true).display();
		
	}

	private static Location loc(Coordinate coordinate) {
		return Location.Builder.newInstance().setCoordinate(coordinate).build();
	}

}