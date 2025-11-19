import addresses from "../data/addresses";

export async function fetchUserAddresses(userId) {
  // Uncomment once backend is ready:
  // const response = await fetch(`${import.meta.env.VITE_API_URL}/users/${userId}/addresses`, {
  //   headers: { Authorization: `Bearer ${token}` },
  // });
  // if (!response.ok) throw new Error("Failed to load addresses");
  // return response.json();

  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(addresses.filter((address) => address.userId === userId));
    }, 300);
  });
}

