package eidd.grp5.presentation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
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

	// Command pattern: each menu option is wrapped as an executable command object
	// so menu flow stays readable when options grow.
	@FunctionalInterface
	private interface MenuCommand {
		void execute();
	}

	// Abstract Factory pattern: this centralizes dependency creation for the default console setup
	// so wiring can change later without changing the UI logic.
	private interface ConsoleDependencyFactory {
		UserService createUserService();

		RoomService createRoomService();

		ReservationService createReservationService();
	}

	private static final class DefaultConsoleDependencyFactory implements ConsoleDependencyFactory {
		@Override
		public UserService createUserService() {
			return new UserService(new UserRepository());
		}

		@Override
		public RoomService createRoomService() {
			return new RoomService(new RoomRepository());
		}

		@Override
		public ReservationService createReservationService() {
			return new ReservationService(new ReservationRepository());
		}
	}

	private final UserService userService;
	private final RoomService roomService;
	private final ReservationService reservationService;

	public ConsoleUI(UserService userService, RoomService roomService, ReservationService reservationService) {
		this.userService = Objects.requireNonNull(userService, "userService must not be null");
		this.roomService = Objects.requireNonNull(roomService, "roomService must not be null");
		this.reservationService = Objects.requireNonNull(reservationService, "reservationService must not be null");
	}

	public static ConsoleUI createDefault() {
		ConsoleDependencyFactory factory = new DefaultConsoleDependencyFactory();
		return new ConsoleUI(
				factory.createUserService(),
				factory.createRoomService(),
				factory.createReservationService());
	}

	public void start() {
		try (Scanner scanner = new Scanner(System.in)) {
			boolean running = true;
			while (running) {
				if (!scanner.hasNextLine()) {
					System.out.println();
					System.out.println("Fermeture de la vue console...");
					break;
				}
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
					case "8" -> showReservationMenu(scanner);
					case "9" -> showAvailabilityMenu(scanner);
					case "10" -> {
						System.out.println("Fermeture de la vue console...");
						running = false;
					}
					default -> System.out.println("Choix invalide. Reessaie.");
				}
			}
		} catch (NoSuchElementException | IllegalStateException e) {
			System.out.println();
			System.out.println("Fermeture de la vue console...");
		}
	}

	private void printMenu() {
		printBlockTitle("Vue Console");
		printMenuSection("General", List.of(
				"1. Afficher les statistiques",
				"10. Quitter"));
		printMenuSection("Utilisateurs et Salles", List.of(
				"2. Ajouter un utilisateur",
				"3. Lister les utilisateurs",
				"4. Ajouter une salle",
				"5. Lister les salles"));
		printMenuSection("Reservations", List.of(
				"6. Creer une reservation simple",
				"7. Lister les reservations",
				"8. Recherche et gestion des reservations",
				"9. Verifier disponibilite et conflits"));
	}

	private void printStats() {
		System.out.println();
		printLineSeparator();
		System.out.println("Statistiques globales");
		printLineSeparator();
		System.out.println("Utilisateurs : " + userService.countUsers());
		System.out.println("Salles       : " + roomService.countRooms());
		System.out.println("Reservations : " + reservationService.getAllReservations().size());
		printLineSeparator();
		System.out.println();
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

		System.out.print("Role (CUSTOMER/ADMIN, vide = CUSTOMER): ");
		String roleInput = scanner.nextLine().trim().toUpperCase();

		User user = new User(name, email);
		if (!roleInput.isEmpty()) {
			try {
				user.setRole(User.Role.valueOf(roleInput));
			} catch (IllegalArgumentException e) {
				System.out.println("Role invalide, CUSTOMER applique par defaut.");
			}
		}

		User created = userService.createUser(user);
		System.out.println("Utilisateur cree avec l'ID: " + created.getId());
	}

	private void listUsers() {
		List<User> users = userService.getAllUsers();
		if (users.isEmpty()) {
			System.out.println("Aucun utilisateur enregistre.");
			System.out.println();
			return;
		}

		System.out.println();
		System.out.println("--- Utilisateurs ---");
		printLineSeparator();
		System.out.println("ID | NOM | EMAIL | ROLE");
		printLineSeparator();
		for (User user : users) {
			String role = user.getRole() == null ? "CUSTOMER" : user.getRole().name();
			System.out.println(user.getId() + " | " + user.getName() + " | " + user.getEmail() + " | role=" + role);
		}
		printLineSeparator();
		System.out.println();
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
			System.out.println();
			return;
		}

		System.out.println();
		System.out.println("--- Salles ---");
		printLineSeparator();
		System.out.println("ID | NOM | CAPACITE | DESCRIPTION");
		printLineSeparator();
		for (Room room : rooms) {
			System.out.println(room.getId() + " | " + room.getName() + " | cap=" + room.getCapacity() + " | " + room.getDescription());
		}
		printLineSeparator();
		System.out.println();
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

		int participantCount = readPositiveIntOrDefault(scanner,
				"Nombre de participants (vide = 1): ",
				1);
		if (participantCount <= 0) {
			System.out.println("Nombre de participants invalide.");
			return;
		}

		System.out.print("Motif (optionnel): ");
		String purposeInput = scanner.nextLine().trim();

		Reservation reservation = new Reservation();
		reservation.setClient(userOpt.get());
		reservation.setRoom(roomOpt.get());
		reservation.setStartDate(startDate);
		reservation.setEndDate(endDate);
		reservation.setParticipantCount(participantCount);
		reservation.setPurpose(purposeInput.isEmpty() ? null : purposeInput);

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
			System.out.println();
			return;
		}

		System.out.println();
		System.out.println("--- Reservations ---");
		printLineSeparator();
		System.out.println("ID | CLIENT | SALLE | DEBUT | FIN | STATUT");
		printLineSeparator();
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
		printLineSeparator();
		System.out.println();
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

	private int readPositiveIntOrDefault(Scanner scanner, String prompt, int defaultValue) {
		System.out.print(prompt);
		String input = scanner.nextLine().trim();
		if (input.isEmpty()) {
			// Empty input keeps the default value for faster data entry.
			return defaultValue;
		}
		try {
			int parsed = Integer.parseInt(input);
			// A negative return value is used as a validation flag by callers.
			return parsed > 0 ? parsed : -1;
		} catch (NumberFormatException e) {
			return -1;
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

	private LocalDate readDate(Scanner scanner, String prompt) {
		while (true) {
			System.out.print(prompt);
			String input = scanner.nextLine().trim();
			try {
				return LocalDate.parse(input);
			} catch (DateTimeParseException e) {
				System.out.println("Format invalide. Exemple: 2026-04-01");
			}
		}
	}

	private String formatDateTime(LocalDateTime dateTime) {
		if (dateTime == null) {
			return "-";
		}
		return dateTime.format(DATE_TIME_FORMAT);
	}

	private void showReservationMenu(Scanner scanner) {
		Map<String, MenuCommand> actions = buildReservationMenuActions(scanner);
		boolean inMenu = true;
		while (inMenu) {
			printBlockTitle("Gestion Reservations");
			printMenuSection("Recherche", List.of(
					"1. Chercher par client",
					"2. Chercher par statut",
					"3. Chercher par reference"));
			printMenuSection("Actions", List.of(
					"4. Confirmer une reservation",
					"5. Annuler une reservation",
					"6. Modifier ma reservation (client)",
					"7. Modifier une reservation (admin)"));
			printMenuSection("Vues", List.of(
					"8. Voir mes reservations a venir",
					"9. Vue admin: reservations par salle",
					"10. Retour au menu principal"));
			System.out.print("Choix: ");
			String choice = scanner.nextLine().trim();

			if ("10".equals(choice)) {
				inMenu = false;
				continue;
			}
			runMenuAction(actions, choice);
		}
	}

	private void showAvailabilityMenu(Scanner scanner) {
		Map<String, MenuCommand> actions = buildAvailabilityMenuActions(scanner);
		boolean inMenu = true;
		while (inMenu) {
			printBlockTitle("Disponibilite et Conflits");
			printMenuSection("Salle specifique", List.of(
					"1. Verifier disponibilite d'une salle",
					"2. Afficher les conflits",
					"3. Voir taux d'occupation d'une salle",
					"4. Voir planning journalier d'une salle"));
			printMenuSection("Vue globale", List.of(
					"5. Voir toutes les salles disponibles maintenant",
					"6. Voir salles disponibles pour un creneau",
					"7. Retour au menu principal"));
			System.out.print("Choix: ");
			String choice = scanner.nextLine().trim();

			if ("7".equals(choice)) {
				inMenu = false;
				continue;
			}
			runMenuAction(actions, choice);
		}
	}

	private Map<String, MenuCommand> buildReservationMenuActions(Scanner scanner) {
		Map<String, MenuCommand> actions = new LinkedHashMap<>();
		actions.put("1", () -> searchReservationsByClient(scanner));
		actions.put("2", () -> searchReservationsByStatus(scanner));
		actions.put("3", () -> searchReservationByReference(scanner));
		actions.put("4", () -> confirmReservationFromMenu(scanner));
		actions.put("5", () -> cancelReservationFromMenu(scanner));
		actions.put("6", () -> modifyOwnReservationFromMenu(scanner));
		actions.put("7", () -> modifyReservationAsAdminFromMenu(scanner));
		actions.put("8", () -> showUpcomingReservationsFromMenu(scanner));
		actions.put("9", this::showAllReservationsByRoomFromMenu);
		return actions;
	}

	private Map<String, MenuCommand> buildAvailabilityMenuActions(Scanner scanner) {
		Map<String, MenuCommand> actions = new LinkedHashMap<>();
		actions.put("1", () -> checkRoomAvailability(scanner));
		actions.put("2", () -> showConflictingReservations(scanner));
		actions.put("3", () -> showRoomOccupancy(scanner));
		actions.put("4", () -> showRoomDailySchedule(scanner));
		actions.put("5", this::showAvailableRoomsNow);
		actions.put("6", () -> showAvailableRoomsForPeriod(scanner));
		return actions;
	}

	private void runMenuAction(Map<String, MenuCommand> actions, String choice) {
		MenuCommand action = actions.get(choice);
		if (action == null) {
			System.out.println("Choix invalide.");
			return;
		}
		action.execute();
	}

	private void modifyOwnReservationFromMenu(Scanner scanner) {
		listUsers();
		long actorUserId = readLong(scanner, "ID client (proprietaire): ");
		Optional<User> actorOpt = userService.getUserById(actorUserId);
		if (actorOpt.isEmpty()) {
			System.out.println("Client introuvable.");
			return;
		}

		listReservations();
		long reservationId = readLong(scanner, "ID reservation a modifier: ");
		Optional<Reservation> reservationOpt = reservationService.getReservationById(reservationId);
		if (reservationOpt.isEmpty()) {
			System.out.println("Reservation non trouvee.");
			return;
		}

		Reservation updatedReservation = copyReservation(reservationOpt.get());
		try {
			updateReservationTimeAndRoom(scanner, updatedReservation);
			reservationService.modifyOwnReservation(actorOpt.get(), updatedReservation);
			System.out.println("Reservation modifiee (client).");
		} catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
			System.out.println("Modification impossible: " + e.getMessage());
		}
	}

	private void modifyReservationAsAdminFromMenu(Scanner scanner) {
		listUsers();
		long actorUserId = readLong(scanner, "ID admin: ");
		Optional<User> actorOpt = userService.getUserById(actorUserId);
		if (actorOpt.isEmpty()) {
			System.out.println("Admin introuvable.");
			return;
		}

		listReservations();
		long reservationId = readLong(scanner, "ID reservation a modifier: ");
		Optional<Reservation> reservationOpt = reservationService.getReservationById(reservationId);
		if (reservationOpt.isEmpty()) {
			System.out.println("Reservation non trouvee.");
			return;
		}

		Reservation updatedReservation = copyReservation(reservationOpt.get());
		try {
			updateReservationTimeAndRoom(scanner, updatedReservation);
			reservationService.modifyReservationAsAdmin(actorOpt.get(), updatedReservation);
			System.out.println("Reservation modifiee (admin).");
		} catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
			System.out.println("Modification impossible: " + e.getMessage());
		}
	}

	private void showUpcomingReservationsFromMenu(Scanner scanner) {
		listUsers();
		long clientId = readLong(scanner, "ID client: ");
		LocalDateTime fromDate = readDateTime(scanner, "A partir de (format yyyy-MM-dd HH:mm): ");

		List<Reservation> upcoming = reservationService.getUpcomingReservationsByClient(clientId, fromDate);
		if (upcoming.isEmpty()) {
			System.out.println("Aucune reservation a venir.");
			return;
		}

		System.out.println("--- Reservations a venir ===");
		for (Reservation reservation : upcoming) {
			printReservationDetails(reservation);
		}
	}

	private void showAllReservationsByRoomFromMenu() {
		listRooms();
		if (roomService.countRooms() == 0) {
			return;
		}

		System.out.println("--- Vue admin: reservations par salle ===");
		for (Room room : roomService.getAllRooms()) {
			if (room.getId() == null) {
				continue;
			}
			System.out.println("Salle " + room.getName() + " (ID=" + room.getId() + ")");
			List<Reservation> reservations = reservationService.getReservationsByRoom(room.getId());
			if (reservations.isEmpty()) {
				System.out.println("  Aucune reservation.");
				continue;
			}
			for (Reservation reservation : reservations) {
				System.out.println("  - [" + reservation.getId() + "] "
						+ formatDateTime(reservation.getStartDate())
						+ " -> " + formatDateTime(reservation.getEndDate()));
			}
		}
	}

	private void showRoomDailySchedule(Scanner scanner) {
		listRooms();
		long roomId = readLong(scanner, "ID salle: ");
		LocalDate date = readDate(scanner, "Date (format yyyy-MM-dd): ");

		List<Reservation> schedule = reservationService.getRoomDailySchedule(roomId, date);
		if (schedule.isEmpty()) {
			System.out.println("Aucune reservation ce jour pour cette salle.");
			return;
		}

		System.out.println("--- Planning journalier ===");
		for (Reservation reservation : schedule) {
			printReservationDetails(reservation);
		}
	}

	private void showAvailableRoomsNow() {
		List<Room> availableRooms = reservationService.getAvailableRoomsNow(
				roomService.getAllRooms(),
				LocalDateTime.now());

		if (availableRooms.isEmpty()) {
			System.out.println("Aucune salle disponible pour le moment.");
			return;
		}

		System.out.println("--- Salles disponibles maintenant ===");
		for (Room room : availableRooms) {
			System.out.println(room.getId() + " | " + room.getName() + " | cap=" + room.getCapacity());
		}
	}

	private void showAvailableRoomsForPeriod(Scanner scanner) {
		LocalDateTime startDate = readDateTime(scanner, "Debut (format yyyy-MM-dd HH:mm): ");
		LocalDateTime endDate = readDateTime(scanner, "Fin (format yyyy-MM-dd HH:mm): ");

		if (!endDate.isAfter(startDate)) {
			System.out.println("La fin doit etre apres le debut.");
			return;
		}

		List<Room> availableRooms = reservationService.getAvailableRoomsForPeriod(
				roomService.getAllRooms(),
				startDate,
				endDate);

		if (availableRooms.isEmpty()) {
			System.out.println("Aucune salle disponible pour ce creneau.");
			return;
		}

		System.out.println("--- Salles disponibles pour le creneau ===");
		for (Room room : availableRooms) {
			System.out.println(room.getId() + " | " + room.getName() + " | cap=" + room.getCapacity());
		}
	}

	private void searchReservationsByClient(Scanner scanner) {
		listUsers();
		long clientId = readLong(scanner, "ID client recherche: ");
		List<Reservation> reservations = reservationService.getReservationsByClient(clientId);
		if (reservations.isEmpty()) {
			System.out.println("Aucune reservation pour ce client.");
			return;
		}
		System.out.println("--- Reservations du client ===");
		for (Reservation reservation : reservations) {
			printReservationDetails(reservation);
		}
	}

	private void searchReservationsByStatus(Scanner scanner) {
		System.out.println("Statuts disponibles: PENDING, CONFIRMED, CANCELLED");
		System.out.print("Statut recherche: ");
		String statusInput = scanner.nextLine().trim().toUpperCase();
		try {
			Reservation.Status status = Reservation.Status.valueOf(statusInput);
			List<Reservation> reservations = reservationService.getReservationsByStatus(status);
			if (reservations.isEmpty()) {
				System.out.println("Aucune reservation avec le statut : " + status);
				return;
			}
			System.out.println("--- Reservations avec statut " + status + " ===");
			for (Reservation reservation : reservations) {
				printReservationDetails(reservation);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Statut invalide.");
		}
	}

	private void searchReservationByReference(Scanner scanner) {
		System.out.print("Reference recherchee: ");
		String reference = scanner.nextLine().trim();
		var reservation = reservationService.getReservationByReference(reference);
		if (reservation.isEmpty()) {
			System.out.println("Reservation non trouvee.");
			return;
		}
		System.out.println("--- Reservation trouvee ===");
		printReservationDetails(reservation.get());
	}

	private void confirmReservationFromMenu(Scanner scanner) {
		listReservations();
		long id = readLong(scanner, "ID reservation a confirmer: ");
		if (reservationService.confirmReservation(id)) {
			System.out.println("Reservation confirmee.");
		} else {
			System.out.println("Reservation non trouvee.");
		}
	}

	private void cancelReservationFromMenu(Scanner scanner) {
		listReservations();
		long id = readLong(scanner, "ID reservation a annuler: ");
		if (reservationService.cancelReservation(id)) {
			System.out.println("Reservation annulee.");
		} else {
			System.out.println("Reservation non trouvee.");
		}
	}

	private void checkRoomAvailability(Scanner scanner) {
		listRooms();
		long roomId = readLong(scanner, "ID salle: ");
		LocalDateTime startDate = readDateTime(scanner, "Debut (format yyyy-MM-dd HH:mm): ");
		LocalDateTime endDate = readDateTime(scanner, "Fin (format yyyy-MM-dd HH:mm): ");

		if (!endDate.isAfter(startDate)) {
			System.out.println("La fin doit etre apres le debut.");
			return;
		}

		if (reservationService.isRoomAvailable(roomId, startDate, endDate)) {
			System.out.println("La salle est DISPONIBLE pour cette periode.");
		} else {
			System.out.println("La salle est OCCUPEE pour cette periode.");
		}
	}

	private void showConflictingReservations(Scanner scanner) {
		listRooms();
		long roomId = readLong(scanner, "ID salle: ");
		LocalDateTime startDate = readDateTime(scanner, "Debut (format yyyy-MM-dd HH:mm): ");
		LocalDateTime endDate = readDateTime(scanner, "Fin (format yyyy-MM-dd HH:mm): ");

		if (!endDate.isAfter(startDate)) {
			System.out.println("La fin doit etre apres le debut.");
			return;
		}

		List<Reservation> conflicts = reservationService.getConflictingReservations(roomId, startDate, endDate);
		if (conflicts.isEmpty()) {
			System.out.println("Aucun conflit pour cette periode.");
			return;
		}
		System.out.println("--- Reservations en conflit ===");
		for (Reservation reservation : conflicts) {
			printReservationDetails(reservation);
		}
	}

	private void showRoomOccupancy(Scanner scanner) {
		listRooms();
		long roomId = readLong(scanner, "ID salle: ");
		LocalDateTime startDate = readDateTime(scanner, "Debut (format yyyy-MM-dd HH:mm): ");
		LocalDateTime endDate = readDateTime(scanner, "Fin (format yyyy-MM-dd HH:mm): ");

		if (!endDate.isAfter(startDate)) {
			System.out.println("La fin doit etre apres le debut.");
			return;
		}

		long occupiedMinutes = reservationService.getRoomOccupancyDuration(roomId, startDate, endDate);
		double percentage = reservationService.getRoomOccupancyPercentage(roomId, startDate, endDate);
		System.out.println("--- Taux d'occupation ===");
		System.out.println("Duree occupee: " + occupiedMinutes + " minutes");
		System.out.printf("Pourcentage d'occupation: %.2f%%\n", percentage);
	}

	private void printReservationDetails(Reservation reservation) {
		String clientName = reservation.getClient() == null ? "-" : reservation.getClient().getName();
		String roomName = reservation.getRoom() == null ? "-" : reservation.getRoom().getName();
		String start = formatDateTime(reservation.getStartDate());
		String end = formatDateTime(reservation.getEndDate());
		String status = reservation.getStatus() == null ? "-" : reservation.getStatus().name();
		String reference = reservation.getReference() == null ? "-" : reservation.getReference();
		int participants = reservation.getParticipantCount();
		String purpose = reservation.getPurpose() == null ? "-" : reservation.getPurpose();

		System.out.println("[" + reference + "] ID=" + reservation.getId());
		System.out.println("  Client: " + clientName);
		System.out.println("  Salle: " + roomName);
		System.out.println("  Periode: " + start + " -> " + end);
		System.out.println("  Participants: " + participants);
		System.out.println("  Motif: " + purpose);
		System.out.println("  Statut: " + status);
		printLineSeparator();
	}

	private void printBlockTitle(String title) {
		System.out.println();
		printLineSeparator();
		System.out.println("=== " + title + " ===");
		printLineSeparator();
		System.out.println();
	}

	private void printMenuSection(String sectionTitle, List<String> options) {
		System.out.println("[" + sectionTitle + "]");
		for (String option : options) {
			System.out.println("  " + option);
		}
		printLineSeparator();
		System.out.println();
	}

	private void printLineSeparator() {
		System.out.println("-----------------------------------------------");
	}

	private Reservation copyReservation(Reservation reservation) {
		// A copy is used to avoid mutating stored data before validation passes.
		Reservation copy = new Reservation();
		copy.setId(reservation.getId());
		copy.setReference(reservation.getReference());
		copy.setClient(reservation.getClient());
		copy.setRoom(reservation.getRoom());
		copy.setStartDate(reservation.getStartDate());
		copy.setEndDate(reservation.getEndDate());
		copy.setCreationDate(reservation.getCreationDate());
		copy.setParticipantCount(reservation.getParticipantCount());
		copy.setPurpose(reservation.getPurpose());
		copy.setStatus(reservation.getStatus());
		return copy;
	}

	private void updateReservationTimeAndRoom(Scanner scanner, Reservation reservation) {
		listRooms();
		long roomId = readLong(scanner, "Nouveau ID salle: ");
		Optional<Room> roomOpt = roomService.getRoomById(roomId);
		if (roomOpt.isEmpty()) {
			throw new IllegalArgumentException("Salle introuvable");
		}

		LocalDateTime startDate = readDateTime(scanner, "Nouveau debut (format yyyy-MM-dd HH:mm): ");
		LocalDateTime endDate = readDateTime(scanner, "Nouvelle fin (format yyyy-MM-dd HH:mm): ");
		if (!endDate.isAfter(startDate)) {
			throw new IllegalArgumentException("La date de fin doit etre apres la date de debut");
		}

		reservation.setRoom(roomOpt.get());
		reservation.setStartDate(startDate);
		reservation.setEndDate(endDate);

		int fallbackParticipants = reservation.getParticipantCount() > 0 ? reservation.getParticipantCount() : 1;
		int participantCount = readPositiveIntOrDefault(
				scanner,
				"Nouveau nombre de participants (vide = conserver: " + fallbackParticipants + "): ",
				fallbackParticipants);
		if (participantCount <= 0) {
			throw new IllegalArgumentException("Nombre de participants invalide");
		}
		reservation.setParticipantCount(participantCount);

		System.out.print("Nouveau motif (vide = conserver): ");
		String purposeInput = scanner.nextLine().trim();
		// Empty text keeps the previous purpose to prevent accidental data loss.
		if (!purposeInput.isEmpty()) {
			reservation.setPurpose(purposeInput);
		}
	}
}
