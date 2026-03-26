package eidd.grp5.presentation;

import java.util.Scanner;

import eidd.grp5.repository.ReservationRepository;
import eidd.grp5.repository.RoomRepository;
import eidd.grp5.repository.UserRepository;
import eidd.grp5.service.ReservationService;
import eidd.grp5.service.RoomService;
import eidd.grp5.service.UserService;

public class ConsoleUI {

	private final UserService userService;
	private final RoomService roomService;
	private final ReservationService reservationService;

	public ConsoleUI(UserService userService, RoomService roomService, ReservationService reservationService) {
		this.userService = userService;
		this.roomService = roomService;
		this.reservationService = reservationService;
	}

	public static ConsoleUI createDefault() {
		UserRepository userRepository = new UserRepository();
		RoomRepository roomRepository = new RoomRepository();
		ReservationRepository reservationRepository = new ReservationRepository();

		return new ConsoleUI(
				new UserService(userRepository),
				new RoomService(roomRepository),
				new ReservationService(reservationRepository));
	}

	public void start() {
		try (Scanner scanner = new Scanner(System.in)) {
			boolean running = true;
			while (running) {
				printMenu();
				System.out.print("Choix: ");
				String input = scanner.nextLine().trim();

				switch (input) {
					case "1":
						printStats();
						break;
					case "2":
						System.out.println("Fermeture de la vue console...");
						running = false;
						break;
					default:
						System.out.println("Choix invalide. Reessaie.");
				}
			}
		}
	}

	private void printMenu() {
		System.out.println();
		System.out.println("=== Vue Console ===");
		System.out.println("1. Afficher les statistiques");
		System.out.println("2. Quitter");
	}

	private void printStats() {
		System.out.println("Utilisateurs : " + userService.countUsers());
		System.out.println("Salles       : " + roomService.countRooms());
		System.out.println("Reservations : " + reservationService.getAllReservations().size());
	}
}
