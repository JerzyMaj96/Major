import React, { useState } from "react";
import type { UserRegister } from "../../types/types";
import { useNavigate } from "react-router-dom";
import { authService } from "../../api/services";

function RegisterPage() {
  const [user, setUser] = useState<UserRegister>({
    name: "",
    email: "",
    password: "",
  });

  const navigate = useNavigate();

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { value, name } = event.target;
    setUser((prevVal) => ({ ...prevVal, [name]: value }));
  };

  const handleUser = async (event: React.SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();

    try {
      const data = await authService.register(user);

      alert(
        "Your user account has been successfully created! Your ID is: " +
          data.id,
      );
      setUser({ name: "", email: "", password: "" });
      navigate("/login");
    } catch (error) {
      if (error instanceof Error) {
        alert("Error: " + error.message);
      } else {
        alert("An unknown error occurred");
      }
    }
  };

  return (
    <div className="register-page">
      <h1>Registration</h1>
      <form onSubmit={handleUser}>
        <input
          type="text"
          name="name"
          placeholder="User name"
          value={user.name}
          onChange={handleChange}
        />
        <input
          type="email"
          name="email"
          placeholder="Email"
          value={user.email}
          onChange={handleChange}
        />
        <input
          type="password"
          name="password"
          placeholder="Password"
          value={user.password}
          onChange={handleChange}
        />
        <button type="submit"> Register</button>
      </form>
    </div>
  );
}

export default RegisterPage;
