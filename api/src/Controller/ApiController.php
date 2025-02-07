<?php

namespace App\Controller;

use App\Entity\Category;
use App\Entity\Image;
use App\Entity\User;
use App\Repository\UserRepository;
use App\Repository\CategoryRepository;
use App\Repository\ImageRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;

class ApiController extends AbstractController
{
    #[Route('/health', name: 'app_health', methods: ['GET'])]
    public function health(): JsonResponse
    {
        $json = ['Symfony API is currently working'];

        return new JsonResponse($json);
    }

    #[Route('/user/register', name: 'app_user_register', methods: ['POST'])]
    public function register(Request $request, UserRepository $userRepository, EntityManagerInterface $em, UserPasswordHasherInterface $passwordHasher): JsonResponse {
        $data = json_decode($request->getContent(), true);

        if (!isset($data['email']) || !isset($data['password'])) {
            return new JsonResponse(['error' => 'Email and password are required'], 400);
        }

        $email = $data['email'];
        $password = $data['password'];

        $user = $userRepository->findOneBy(['email' => $email]);

        if ($user) {
            return new JsonResponse(['error' => 'User already exists'], 400);
        }

        $user = new User();
        $user->setEmail($email);
        $user->setPassword($passwordHasher->hashPassword($user, $password));

        $em->persist($user);
        $em->flush();

        $json = [
            'id' => $user->getId(),
            'email' => $user->getEmail(),
        ];

        return new JsonResponse($json);
    }

    #[Route('/user/login', name: 'app_user_login', methods: ['POST'])]
    public function login(Request $request, UserRepository $userRepository, UserPasswordHasherInterface $passwordHasher): JsonResponse {
        $data = json_decode($request->getContent(), true);

        if (!isset($data['email']) || !isset($data['password'])) {
            return new JsonResponse(['error' => 'Email and password are required'], 400);
        }

        $email = $data['email'];
        $password = $data['password'];

        $user = $userRepository->findOneBy(['email' => $email]);

        if (!$user) {
            return new JsonResponse(['error' => 'Invalid credentials'], 400);
        }

        if (!$passwordHasher->isPasswordValid($user, $password)) {
            return new JsonResponse(['error' => 'Invalid credentials'], 400);
        }

        $json = [
            'id' => $user->getId(),
            'email' => $user->getEmail(),
        ];

        return new JsonResponse($json);
    }


    #[Route('/image/{image_id}/get', name: 'app_image_get', methods: ['GET'])]
    public function get(string $image_id, ImageRepository $imageRepository): JsonResponse
    {
        $image = $imageRepository->findOneBy(['imageId' => $image_id]);

        if (!$image) {
            return new JsonResponse(null);
        }

        $json = [
            'id' => $image->getImageId(),
            'userId' => $image->getUser()->getId(),
            'url' => $image->getUrl(),
            'categories' => array_map(function ($category) {
                return [
                    'id' => $category->getId(),
                    'label' => $category->getLabel(),
                ];
            }, $image->getCategories()->toArray()),
        ];

        return new JsonResponse($json);
    }

    #[Route('/image/all', name: 'app_image_all', methods: ['GET'])]
    public function all(ImageRepository $imageRepository): JsonResponse
    {
        $images = $imageRepository->getAll();

        if (empty($images)) {
            return new JsonResponse([]);
        }

        $json = array_map(function ($image) {
            return [
                'id' => $image->getImageId(),
                'userId' => $image->getUser()->getId(),
                'url' => $image->getUrl(),
                'categories' => array_map(function ($category) {
                    return [
                        'id' => $category->getId(),
                        'label' => $category->getLabel(),
                    ];
                }, $image->getCategories()->toArray()),
            ];
        }, $images);

        return new JsonResponse($json);
    }

    #[Route('/image/upload', name: 'app_image_upload', methods: ['POST'])]
    public function upload(Request $request, EntityManagerInterface $entityManager, CategoryRepository $categoryRepository, ImageRepository $imageRepository, UserRepository $userRepository): JsonResponse {
        $uploadedFile = $request->files->get('image');
    
        if (!$uploadedFile || !$uploadedFile->isValid()) {
            return new JsonResponse(['error' => 'Invalid file upload'], 400);
        }
    
        $imageCategoryData = $request->request->get('imageCategory');
    
        if (!$imageCategoryData) {
            return new JsonResponse(['error' => 'Image category data is required'], 400);
        }
    
        $imageCategoryArray = json_decode($imageCategoryData, true);
    
        if (!is_array($imageCategoryArray) || !isset($imageCategoryArray['imageId']) || !isset($imageCategoryArray['userId']) || !isset($imageCategoryArray['categories'])) {
            return new JsonResponse(['error' => 'Invalid image category format'], 400);
        }
    
        $imageId = $imageCategoryArray['imageId'];
        $userId = $imageCategoryArray['userId'];
        $categoriesArray = $imageCategoryArray['categories'];

        if (!$imageId) {
            return new JsonResponse(['error' => 'Image ID is required'], 400);
        }

        if (!$userId) {
            return new JsonResponse(['error' => 'User ID is required'], 400);
        }
    
        $user = $userRepository->find($userId);
    
        if (!$user) {
            return new JsonResponse(['error' => 'User not found'], 404);
        }
    
        if (!is_array($categoriesArray)) {
            return new JsonResponse(['error' => 'Invalid categories format'], 400);
        }
    
        $categories = [];

        foreach ($categoriesArray as $categoryData) {
            if (!isset($categoryData['label'])) {
                continue;
            }
    
            $label = $categoryData['label'];
            $category = $categoryRepository->findOneBy(['label' => $label]);
    
            if (!$category) {
                $category = new Category();
                $category->setLabel($label);
                $entityManager->persist($category);
            }
    
            $categories[] = $category;
        }
    
        $uploadDir = $this->getParameter('upload_directory');

        if (!is_dir($uploadDir)) {
            if (!mkdir($uploadDir, 0755, true) && !is_dir($uploadDir)) {
                return new JsonResponse(['error' => 'Failed to create upload directory'], 500);
            }
        }
    
        $newFilename = bin2hex(random_bytes(16)).'.'.$uploadedFile->guessExtension();
    
        try {
            $uploadedFile->move($uploadDir, $newFilename);
        } catch (FileException) {
            return new JsonResponse(['error' => 'Failed to upload file'], 500);
        }
    
        $uploadUrl = $this->getParameter('upload_url');
        $imageUrl = $uploadUrl.'/'.$newFilename;
    
        $image = $imageRepository->findOneBy(['imageId' => $imageId]);

        if ($image) {
            return new JsonResponse(['error' => 'This file is already uploaded'], 500);
        }

        $image = new Image();
        $image->setImageId($imageId);
        $image->setUser($user);
        $image->setUrl($imageUrl);
    
        foreach ($categories as $category) {
            $image->addCategory($category);
        }
    
        $entityManager->persist($image);
        $entityManager->flush();
    
        return new JsonResponse(['success' => 'Image uploaded successfully']);
    }

    #[Route('/image/{image_id}/delete', name: 'app_image_delete', methods: ['DELETE'])]
    public function delete(string $image_id, ImageRepository $imageRepository, EntityManagerInterface $entityManager): JsonResponse
    {
        $image = $imageRepository->findOneBy(['imageId' => $image_id]);

        if (!$image) {
            return new JsonResponse(['error' => 'Image not found'], 404);
        }

        $uploadDir = $this->getParameter('upload_directory');
        $imageFilename = basename(parse_url($image->getUrl(), PHP_URL_PATH));
        $imagePath = $uploadDir.'/'.$imageFilename;

        if (file_exists($imagePath)) {
            if (!unlink($imagePath)) {
                return new JsonResponse(['error' => 'Failed to delete image file'], 500);
            }
        }

        $entityManager->remove($image);
        $entityManager->flush();

        return new JsonResponse(['success' => 'Image deleted successfully']);
    }
}
