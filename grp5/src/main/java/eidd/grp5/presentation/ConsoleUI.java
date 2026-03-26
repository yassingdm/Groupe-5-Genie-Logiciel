package eidd.grp5.presentation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import eidd.grp5.model.Reservation;
import eidd.grp5.model.Room;
import eidd.grp5.model.User;
import eidd.grp5.repository.ReservationRepository;
import eidd.grp5.repository.RoomRepository;
import eidd.grp5.repository.UserRepository;
import eidd.grp5.service.ReservationService;
import eidd.grp5.service.RoomService;
import eidd.grp5.service.UserService;

public class ConsoleUI {

	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
					case "1" -> printStats();
					case "2" -> createUser(scanner);
					case "3" -> listUsers();
					case "4" -> createRoom(scanner);
					case "5" -> listRooms();
					case "6" -> createReservation(scanner);
					case "7" -> listReservations();
					case "8" -> {
						System.out.println("Fermeture de la vue console...");
						running = false;
					}
					default -> System.out.println("Choix invalide. Reessaie.");
				}
			}
		}
	}

	private void printMenu() {
		System.out.println();
		System.out.println("=== Vue Console ===");
		System.out.println("1. Afficher les statistiques");
		System.out.println("2. Ajouter un utilisateur");
		System.out.println("3. Lister les utilisateurs");
		System.out.println("4. Ajouter une salle");
		System.out.println("5. Lister les salles");
		System.out.println("6. Creer une reservation simple");
		System.out.println("7. Lister les reservations");
		System.out.println("8. Quitter");
	}

	private void printStats() {
		System.out.println("Utilisateurs : " + userService.countUsers());
		System.out.println("Salles       : " + roomService.countRooms());
		System.out.println("Reservations : " + reservationService.getAllReservations().size());
	}

	private void createUser(Scanner scanner) {
		System.out.print("Nom utilisateur: ");
		String name = scanner.nextLine().trim();

		System.out.print("Email utilisateur: ");
		String email = scanner.nextLine().trim();

		if (name.isEmpty() || email.isEmpty()) {
			System.out.println("Nom et email sont obligatoires.");
			return;
		}

		User created = userService.createUser(new User(name, email));
		System.out.println("Utilisateur cree avec l'ID: " + created.getId());
	}

	private void listUsers() {
		List<User> users = userService.getAllUsers();
		if (users.isEmpty()) {
			System.out.println("Aucun utilisateur enregistre.");
			return;
		}

		System.out.println("--- Utilisateurs ---");
		for (User user : users) {
			System.out.println(user.getId() + " | " + user.getName() + " | " + user.getEmail());
		}
	}

	private void createRoom(Scanner scanner) {
		System.out.print("Nom salle: ");
		String name = scanner.nextLine().trim();

		System.out.print("Capacite: ");
		String capacityInput = scanner.nextLine().trim();

		System.out.print("Description: ");
		String description = scanner.nextLine().trim();

		if (name.isEmpty() || capacityInput.isEmpty()) {
			System.out.println("Nom et capacite sont obligatoires.");
			return;
		}

		int capacity;
		try {
			capacity = Integer.parseInt(capacityInput);
		} catch (NumberFormatException e) {
			System.out.println("Capacite invalide.");
			return;
		}

		if (capacity <= 0) {
			System.out.println("La capacite doit etre > 0.");
			return;
		}

		Room room = new Room(0, name, capacity, description);
		room.setId(null);
		Room created = roomService.createRoom(room);
		System.out.println("Salle creee avec l'ID: " + created.getId());
	}

	private void listRooms() {
		List<Room> rooms = roomService.getAllRooms();
		if (rooms.isEmpty()) {
			System.out.println("Aucune salle enregistree.");
			return;
		}

		System.out.println("--- Salles ---");
		for (Room room : rooms) {
			System.out.println(room.getId() + " | " + room.getName() + " | cap=" + room.getCapacity() + " | " + room.getDescription());
		}
	}

	private void createReservation(Scanner scanner) {
		if (userService.countUsers() == 0 || roomService.countRooms() == 0) {
			System.out.println("Ajoute d'abord au moins un utilisateur et une salle.");
			return;
		}

		listUsers();
		long userId = readLong(scanner, "ID utilisateur: ");

		listRooms();
		long roomId = readLong(scanner, "ID salle: ");

		LocalDateTime startDate = readDateTime(scanner,
				"Debut (format yyyy-MM-ddTHH:mm ou yyyy-MM-dd HH:mm): ");
		System.out.println("Date de debut enregistree.");
		LocalDateTime endDate = readDateTime(scanner,
				"Fin (format yyyy-MM-ddTHH:mm ou yyyy-MM-dd HH:mm): ");

		if (!endDate.isAfter(startDate)) {
			System.out.println("La date de fin doit etre apres la date de debut.");
			return;
		}

		Optional<User> userOpt = userService.getUserById(userId);
		Optional<Room> roomOpt = roomService.getRoomById(roomId);

		if (userOpt.isEmpty() || roomOpt.isEmpty()) {
			System.out.println("Utilisateur ou salle introuvable.");
			return;
		}

		Reservation reservation = new Reservation();
		reservation.setClient(userOpt.get());
		reservation.setRoom(roomOpt.get());
		reservation.setStartDate(startDate);
		reservation.setEndDate(endDate);
		reservation.setParticipantCount(1);

		try {
			Reservation created = reservationService.createReservation(reservation);
			System.out.println("Reservation creee avec l'ID: " + created.getId());
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Impossible de creer la reservation: " + e.getMessage());
		}
	}

	private void listReservations() {
		List<Reservation> reservations = reservationService.getAllReservations();
		if (reservations.isEmpty()) {
			System.out.println("Aucune reservation enregistree.");
			return;
		}

		System.out.println("--- Reservations ---");
		for (Reservation reservation : reservations) {
			String clientName = reservation.getClient() == null ? "-" : reservation.getClient().getName();
			String roomName = reservation.getRoom() == null ? "-" : reservation.getRoom().getName();
			String start = formatDateTime(reservation.getStartDate());
			String end = formatDateTime(reservation.getEndDate());
			String status = reservation.getStatus() == null ? "-" : reservation.getStatus().name();

			System.out.println(
					reservation.getId() + " | client=" + clientName + " | salle=" + roomName + " | debut=" + start
							+ " | fin=" + end + " | statut=" + status);
		}
	}

	private long readLong(Scanner scanner, String prompt) {
		while (true) {
			System.out.print(prompt);
			String input = scanner.nextLine().trim();
			try {
				return Long.parseLong(input);
			} catch (NumberFormatException e) {
				System.out.println("Valeur invalide, entre un nombre entier.");
			}
		}
	}

	private LocalDateTime readDateTime(Scanner scanner, String prompt) {
		while (true) {
			System.out.print(prompt);
			String input = scanner.nextLine().trim();
			try {
				if (input.contains("T")) {
					return LocalDateTime.parse(input);
				}
				return LocalDateTime.parse(input, DATE_TIME_FORMAT);
			} catch (DateTimeParseException e) {
				System.out.println("Format invalide. Exemples: 2026-04-01T10:00 ou 2026-04-01 10:00");
			}
		}
	}

	private String formatDateTime(LocalDateTime dateTime) {
		if (dateTime == null) {
			return "-";
		}
		return dateTime.format(DATE_TIME_FORMAT);
	}
}
