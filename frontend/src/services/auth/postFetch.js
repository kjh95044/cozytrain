export default async function fetchPost(url, data) {
  try {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API}${url}`, {
      method: "POST",
      body: JSON.stringify(data),
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);

    const responseData = await response.json();
    return responseData;
  } catch (error) {
    console.log(error);
  }
}
