import type { UserLogin } from "../../types/types";
import { useFormState } from "../../hooks/useFormState";
import { useNavigate } from "react-router";
import { useAuth } from "../../hooks/useAuth";

function LoginPage() {
  const { values: credentials, handleChange } = useFormState<UserLogin>({
    identifier: "",
    password: "",
  });

  const navigate = useNavigate();
  const {login} = useAuth();

  const handleSubmit = async (event: React.SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();

    try {
      await login(credentials);
      navigate("/dashboard");
    } catch (error) {
      if (error instanceof Error) {
        alert("Error: " + error.message);
      } else {
        alert("An unknown error occurred");
      }
    }
  };

  return (
    <div>
      <h1>Login</h1>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          name="identifier"
          placeholder="Username"
          value={credentials.identifier}
          onChange={handleChange}
        />
        <input
          type="password"
          name="password"
          placeholder="Password"
          value={credentials.password}
          onChange={handleChange}
        />
        <button type="submit">Login</button>
        <button type="button" onClick={() => navigate("/register")}>
          Register
        </button>
      </form>
    </div>
  );
}

export default LoginPage;
