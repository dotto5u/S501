<?php

namespace App\DataFixtures;

use App\Entity\Image;
use Doctrine\Bundle\FixturesBundle\Fixture;
use Doctrine\Persistence\ObjectManager;
use Doctrine\Common\DataFixtures\DependentFixtureInterface;

class ImageFixtures extends Fixture implements DependentFixtureInterface
{
    public const IMAGE_REF_PREFIX = 'image_';

    public function load(ObjectManager $manager): void
    {
        $imagesData = [
            [
                'id' => 1,
                'url' => 'https://images.unsplash.com/photo-1607116692929-ece4c84a5992',
                'categories_ref' => ['category_1', 'category_2'],
            ],
            [
                'id' => 2,
                'url' => 'https://images.unsplash.com/photo-1607116692929-ece4c84a5992',
                'categories_ref' => ['category_3', 'category_4'],
            ],
            [
                'id' => 3,
                'url' => 'https://images.unsplash.com/photo-1607116692929-ece4c84a5992',
                'categories_ref' => ['category_5', 'category_6'],
            ],
            [
                'id' => 4,
                'url' => 'https://images.unsplash.com/photo-1607116692929-ece4c84a5992',
                'categories_ref' => ['category_1', 'category_7'],
            ],
            [
                'id' => 5,
                'url' => 'https://images.unsplash.com/photo-1607116692929-ece4c84a5992',
                'categories_ref' => ['category_1', 'category_8'],
            ],
            [
                'id' => 6,
                'url' => 'https://images.unsplash.com/photo-1607116692929-ece4c84a5992',
                'categories_ref' => ['category_1', 'category_2', 'category_4'],
            ],
            [
                'id' => 7,
                'url' => 'https://images.unsplash.com/photo-1607116692929-ece4c84a5992',
                'categories_ref' => ['category_1', 'category_6'],
            ],
            [
                'id' => 8,
                'url' => 'https://images.unsplash.com/photo-1607116692929-ece4c84a5992',
                'categories_ref' => ['category_3', 'category_5', 'category_4'],
            ],
        ];                

        foreach ($imagesData as $data) {
            $image = new Image();
            $image->setUrl($data['url']);

            foreach ($data['categories_ref'] as $categoryRef) {
                $image->addCategory($this->getReference($categoryRef));
            }

            $manager->persist($image);
            $this->addReference(self::IMAGE_REF_PREFIX . $data['id'], $image);
        }

        $manager->flush();
    }

    public function getDependencies(): array
    {
        return [
            CategoryFixtures::class,
        ];
    }
}